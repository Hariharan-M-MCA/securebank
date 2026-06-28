package com.securebank.dto.response;

import lombok.Data;

@Data
public class FraudCheckResponse {
    private Long transactionId;
    private Double fraudScore;
    private Boolean isFraud;
    private String reason;
}
