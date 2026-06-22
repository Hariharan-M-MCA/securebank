package com.securebank.service;

import com.securebank.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");

    @KafkaListener(topics = "transaction-events", groupId = "securebank-group")
    public void analyzeTransaction(TransactionEvent event) {
        log.info("Analyzing transaction: {} for account: {}", event.getTransactionId(), event.getAccountNumber());

        boolean isFraudulent = false;
        String reason = "";

        if (event.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            isFraudulent = true;
            reason = "High value transaction: " + event.getAmount();
        }

        if (isFraudulent) {
            log.warn("FRAUD ALERT! Transaction ID: {} | Account: {} | Reason: {}",
                    event.getTransactionId(), event.getAccountNumber(), reason);
        } else {
            log.info("Transaction {} passed fraud check", event.getTransactionId());
        }
    }
}
