package com.securebank.service;

import com.securebank.dto.request.FraudCheckRequest;
import com.securebank.dto.response.FraudCheckResponse;
import com.securebank.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    private final RestClient restClient;

    @Value("${fraud.detection.service.url}")
    private String fraudServiceUrl;

    @KafkaListener(topics = "transaction-events", groupId = "securebank-group")
    public void analyzeTransaction(TransactionEvent event) {
        log.info("Analyzing transaction: {} for account: {}", event.getTransactionId(), event.getAccountNumber());

        try {
            FraudCheckRequest request = new FraudCheckRequest(
                    event.getTransactionId(),
                    event.getAmount().doubleValue(),
                    event.getTransactionType(),
                    event.getAccountNumber(),
                    event.getEmail()
            );

            FraudCheckResponse response = restClient.post()
                    .uri(fraudServiceUrl + "/predict")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(FraudCheckResponse.class);

            if (response != null && response.getIsFraud()) {
                log.warn("FRAUD ALERT! Transaction ID: {} | Score: {} | Reason: {}",
                        response.getTransactionId(), response.getFraudScore(), response.getReason());
            } else if (response != null) {
                log.info("Transaction {} passed fraud check. Score: {}",
                        response.getTransactionId(), response.getFraudScore());
            }

        } catch (Exception e) {
            log.error("Fraud detection service unavailable: {}", e.getMessage());
        }
    }
}
