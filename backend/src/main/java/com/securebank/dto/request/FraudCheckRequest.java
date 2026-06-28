package com.securebank.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FraudCheckRequest {
    private Long transactionId;
    private Double amount;
    private String transactionType;
    private String accountNumber;
    private String email;
}
