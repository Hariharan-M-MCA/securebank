package com.securebank.dto.response;

import com.securebank.entity.Transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime createdAt;
}
