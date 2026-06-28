package com.securebank.service;

import com.securebank.dto.request.CreateAccountRequest;
import com.securebank.dto.response.AccountResponse;
import com.securebank.entity.Account;
import com.securebank.entity.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @CacheEvict(value = "accounts", key = "#email")
    public AccountResponse createAccount(CreateAccountRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUser(user);

        accountRepository.save(account);

        return mapToResponse(account);
    }

    @Cacheable(value = "accounts", key = "#email")
    public List<AccountResponse> getUserAccounts(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return accountRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateAccountNumber() {
        String accountNumber;

        do {
            accountNumber = String.valueOf(
                    1000000000L + new Random().nextInt(900000000)
            );
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getIsActive(),
                account.getCreatedAt()
        );
    }
}