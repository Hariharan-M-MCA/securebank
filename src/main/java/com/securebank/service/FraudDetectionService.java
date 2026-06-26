package com.securebank.service;

import com.securebank.dto.request.FraudCheckRequest;
import com.securebank.dto.response.FraudCheckResponse;
import com.securebank.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    private final RestClient restClient;

    @Value("${fraud.detection.service.url}")
    private String fraudServiceUrl;

    @KafkaListener(topics = "transaction-events", groupId = "securebank-group")
    @Retryable(
            retryFor = ResourceAccessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void analyzeTransaction(TransactionEvent event) {

        log.info("====================================================");
        log.info("Starting fraud analysis");
        log.info("Transaction ID: {}", event.getTransactionId());
        log.info("Account Number: {}", event.getAccountNumber());
        log.info("====================================================");

        FraudCheckRequest request = new FraudCheckRequest(
                event.getTransactionId(),
                event.getAmount().doubleValue(),
                event.getTransactionType(),
                event.getAccountNumber(),
                event.getEmail()
        );

        log.info("Sending request to FastAPI...");

        FraudCheckResponse response = restClient.post()
                .uri(fraudServiceUrl + "/predict")
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(FraudCheckResponse.class);

        log.info("Response received from FastAPI.");

        if (response != null && response.getIsFraud()) {

            log.warn("====================================================");
            log.warn("FRAUD DETECTED");
            log.warn("Transaction ID : {}", response.getTransactionId());
            log.warn("Fraud Score    : {}", response.getFraudScore());
            log.warn("Reason         : {}", response.getReason());
            log.warn("====================================================");

        } else if (response != null) {

            log.info("====================================================");
            log.info("Transaction Approved");
            log.info("Transaction ID : {}", response.getTransactionId());
            log.info("Fraud Score    : {}", response.getFraudScore());
            log.info("====================================================");

        } else {

            log.warn("FastAPI returned an empty response.");
        }
    }

    @Recover
    public void recover(ResourceAccessException ex, TransactionEvent event) {

        log.error("====================================================");
        log.error("Fraud Detection Service is unavailable.");
        log.error("Transaction ID : {}", event.getTransactionId());
        log.error("Account Number : {}", event.getAccountNumber());
        log.error("Retry attempts exhausted (3 attempts).");
        log.error("Reason : {}", ex.getMessage());
        log.error("Skipping fraud analysis.");
        log.error("====================================================");
    }
}