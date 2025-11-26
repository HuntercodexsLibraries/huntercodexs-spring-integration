package com.huntercodexs.integration.kafka.producer.sender;

import com.huntercodexs.integration.kafka.producer.process.KakfaProducerIntegrationProcess;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.huntercodexs.integration.kafka.producer.constants.KafkaProducerIntegrationConstants.*;

@Component
@RequiredArgsConstructor
public class KafkaProducerIntegration {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerIntegration.class);

    @Value("${"+ KAFKA_PRODUCER_APP_CONFIG +".enabled}")
    private boolean kafkaEnabled;

    @Value("${"+ KAFKA_PRODUCER_SPRING_APP_CONFIG +".cluster-topic-name:"+TOPIC_DEFAULT+"}")
    private String topicName;

    private final @Nullable KafkaTemplate<String, String> kafkaTemplate;
    private final List<KakfaProducerIntegrationProcess> processors;

    public void send(Object message, String producerName, String topicNameOverride) {

        if (!kafkaEnabled) {
            log.warn("Kafka integration is disabled. Message not sent: {}", message);
            throw new IllegalStateException("Kafka integration is disabled.");
        }

        KakfaProducerIntegrationProcess strategy = processors.stream()
                .filter(producer -> producer.supports(producerName))
                .findFirst()
                .orElse(null);

        if (strategy == null) {
            log.error("No Kafka producer found for name: {}", producerName);
            return;
        }

        if (topicNameOverride != null && !topicNameOverride.isBlank()) {
            topicName = topicNameOverride;
        }

        try {

            String mensagemJson = strategy.processMessage(message);

            if (mensagemJson == null) {
                log.warn("Processed message is null for producer: {} | message: {}", producerName, message);
                mensagemJson = String.valueOf(message);
            }

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, mensagemJson);

            HashMap<String, String> hashMap = strategy.producerRecord(message);
            if (hashMap != null) {
                hashMap.forEach((k, v) ->
                        producerRecord.headers().add(k, v.getBytes(StandardCharsets.UTF_8))
                );
            }

            log.info("Sending message to topic '{}': {}", topicName, message);

            assert kafkaTemplate != null;
            final var retornoKafka = kafkaTemplate.send(producerRecord);

            SendResult<String, String> result = retornoKafka.get();

            log.info("Message sent successfully to topic: {} | data: {} | offset: {} | partition: {}",
                    topicName,
                     mensagemJson,
                     result.getRecordMetadata().offset(),
                     result.getRecordMetadata().partition());

        } catch (InterruptedException ie) {

            Thread.currentThread().interrupt();
            log.error("Thread interrupted while sending message to topic: {} | data: {}", topicName, message, ie);

        } catch (ExecutionException ee) {

            log.error("Error sending message to topic: {} | data: {} | cause: {}", topicName, message, ee.getCause(), ee);
        }
    }
}
