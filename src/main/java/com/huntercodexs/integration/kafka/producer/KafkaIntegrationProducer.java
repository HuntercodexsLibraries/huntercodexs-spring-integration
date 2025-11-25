package com.huntercodexs.integration.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huntercodexs.integration.kafka.process.KakfaIntegrationProcessor;
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

import static com.huntercodexs.integration.constants.IntegrationConstants.KAFKA_APP_CONFIG;
import static com.huntercodexs.integration.constants.IntegrationConstants.KAFKA_SPRING_APP_CONFIG;

@Component
@RequiredArgsConstructor
public class KafkaIntegrationProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaIntegrationProducer.class);

    @Value("${"+KAFKA_SPRING_APP_CONFIG+".cluster.topic-name}")
    private String topicName;

    @Value("${"+KAFKA_APP_CONFIG+".enabled}")
    private boolean kafkaEnabled;

    private final @Nullable KafkaTemplate<String, String> kafkaTemplate;
    private final List<KakfaIntegrationProcessor> processors;

    public void send(Object message, String producerName) throws JsonProcessingException {

        if (!kafkaEnabled) {
            log.warn("Kafka integration is disabled. Message not sent: {}", message);
            throw new IllegalStateException("Kafka integration is disabled.");
        }

        KakfaIntegrationProcessor strategy = processors.stream()
                .filter(producer -> producer.supports(producerName))
                .findFirst()
                .orElse(null);

        if (strategy == null) {
            log.error("No Kafka producer found for name: {}", producerName);
            return;
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
