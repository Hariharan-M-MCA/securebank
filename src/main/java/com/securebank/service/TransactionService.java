package com.securebank.service;

import com.securebank.dto.request.TransactionRequest;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Transaction;
import com.securebank.entity.Transaction.TransactionType;
import com.securebank.event.TransactionEvent;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request, String email) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to account");
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setCreatedAt(LocalDateTime.now());

        if (request.getTransactionType() == TransactionType.DEPOSIT) {
            account.setBalance(account.getBalance().add(request.getAmount()));
            transaction.setToAccount(account);
            accountRepository.save(account);

        } else if (request.getTransactionType() == TransactionType.WITHDRAW) {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Insufficient balance");
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            transaction.setFromAccount(account);
            accountRepository.save(account);

        } else if (request.getTransactionType() == TransactionType.TRANSFER) {
            if (request.getToAccountNumber() == null) {
                throw new RuntimeException("Destination account number is required for transfer");
            }
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Insufficient balance");
            }
            Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .orElseThrow(() -> new RuntimeException("Destination account not found"));

            account.setBalance(account.getBalance().subtract(request.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
            transaction.setFromAccount(account);
            transaction.setToAccount(toAccount);
            accountRepository.save(account);
            accountRepository.save(toAccount);
        }

        transactionRepository.save(transaction);

        TransactionEvent event = new TransactionEvent(
                transaction.getId(),
                request.getAccountNumber(),
                request.getTransactionType().name(),
                request.getAmount(),
                email
        );
        kafkaProducerService.publishTransactionEvent(event);

        return mapToResponse(transaction);
    }

    @Transactional
    public List<TransactionResponse> getTransactionHistory(String accountNumber, String email) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to account");
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
                transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null,
                transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null,
                transaction.getCreatedAt()
        );
    }
}
