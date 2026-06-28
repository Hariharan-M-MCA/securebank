package com.securebank.dto.response;

import com.securebank.entity.Account.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AccountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private Boolean isActive;
    private LocalDateTime createdAt;
}