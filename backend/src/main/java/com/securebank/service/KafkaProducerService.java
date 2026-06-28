package com.securebank.service;

import com.securebank.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void publishTransactionEvent(TransactionEvent event) {

        log.info(
                "Publishing transaction event to Kafka | Transaction ID: {} | Account: {}",
                event.getTransactionId(),
                event.getAccountNumber()
        );

        kafkaTemplate.send("transaction-events", event);

        log.info(
                "Kafka event published successfully | Transaction ID: {}",
                event.getTransactionId()
        );
    }
}