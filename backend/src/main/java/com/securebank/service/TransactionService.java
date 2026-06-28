package com.securebank.service;

import com.securebank.dto.request.TransactionRequest;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Transaction;
import com.securebank.entity.Transaction.TransactionType;
import com.securebank.event.TransactionEvent;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.InsufficientBalanceException;
import com.securebank.exception.UnauthorizedException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request, String email) {

        log.info(
                "Transaction request received | Account: {} | Type: {} | Amount: {}",
                request.getAccountNumber(),
                request.getTransactionType(),
                request.getAmount()
        );

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized access to account");
        }

        log.info("Account verified: {}", account.getAccountNumber());

        Transaction transaction = new Transaction();
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setCreatedAt(LocalDateTime.now());

        if (request.getTransactionType() == TransactionType.DEPOSIT) {

            account.setBalance(account.getBalance().add(request.getAmount()));
            transaction.setToAccount(account);
            accountRepository.save(account);

            log.info(
                    "Deposit successful | Account: {} | New Balance: {}",
                    account.getAccountNumber(),
                    account.getBalance()
            );

        }

        else if (request.getTransactionType() == TransactionType.WITHDRAW) {

            log.info(
                    "Withdrawal requested | Account: {} | Amount: {}",
                    account.getAccountNumber(),
                    request.getAmount()
            );

            if (account.getBalance().compareTo(request.getAmount()) < 0) {

                log.warn(
                        "Insufficient balance | Account: {} | Available: {} | Requested: {}",
                        account.getAccountNumber(),
                        account.getBalance(),
                        request.getAmount()
                );

                throw new InsufficientBalanceException("Insufficient balance");
            }

            account.setBalance(account.getBalance().subtract(request.getAmount()));
            transaction.setFromAccount(account);
            accountRepository.save(account);

            log.info(
                    "Withdrawal successful | Account: {} | Remaining Balance: {}",
                    account.getAccountNumber(),
                    account.getBalance()
            );
        }

        else if (request.getTransactionType() == TransactionType.TRANSFER) {

            if (request.getToAccountNumber() == null) {
                throw new RuntimeException("Destination account number is required for transfer");
            }

            log.info(
                    "Transfer initiated | From: {} | To: {} | Amount: {}",
                    account.getAccountNumber(),
                    request.getToAccountNumber(),
                    request.getAmount()
            );

            if (account.getBalance().compareTo(request.getAmount()) < 0) {

                log.warn(
                        "Insufficient balance | Account: {} | Available: {} | Requested: {}",
                        account.getAccountNumber(),
                        account.getBalance(),
                        request.getAmount()
                );

                throw new InsufficientBalanceException("Insufficient balance");
            }

            Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

            account.setBalance(account.getBalance().subtract(request.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            transaction.setFromAccount(account);
            transaction.setToAccount(toAccount);

            accountRepository.save(account);
            accountRepository.save(toAccount);

            log.info(
                    "Transfer completed | From: {} | To: {} | Amount: {}",
                    account.getAccountNumber(),
                    toAccount.getAccountNumber(),
                    request.getAmount()
            );
        }

        transactionRepository.save(transaction);

        log.info(
                "Transaction saved successfully | Transaction ID: {}",
                transaction.getId()
        );

        TransactionEvent event = new TransactionEvent(
                transaction.getId(),
                request.getAccountNumber(),
                request.getTransactionType().name(),
                request.getAmount(),
                email
        );

        log.info(
                "Publishing transaction event to Kafka | Transaction ID: {}",
                transaction.getId()
        );

        kafkaProducerService.publishTransactionEvent(event);

        log.info("Transaction event published successfully.");

        return mapToResponse(transaction);
    }

    @Transactional
    public List<TransactionResponse> getTransactionHistory(String accountNumber, String email) {

        log.info("Fetching transaction history for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized access to account");
        }

        return transactionRepository.findByAccount(account)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getFromAccount() != null
                        ? transaction.getFromAccount().getAccountNumber()
                        : null,
                transaction.getToAccount() != null
                        ? transaction.getToAccount().getAccountNumber()
                        : null,
                transaction.getCreatedAt()
        );
    }
}