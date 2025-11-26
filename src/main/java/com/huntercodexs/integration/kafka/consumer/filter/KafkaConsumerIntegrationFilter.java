package com.huntercodexs.integration.kafka.consumer.filter;

import com.huntercodexs.integration.kafka.consumer.process.KakfaConsumerIntegrationProcess;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Qualifier("kafkaConsumerIntegrationFilter")
public class KafkaConsumerIntegrationFilter<K, V> implements RecordFilterStrategy<K, V> {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerIntegrationFilter.class);

    private final List<KakfaConsumerIntegrationProcess> consumers;

    @Override
    public boolean filter(ConsumerRecord<K, V> record) {
        Headers headers = record.headers();
        V value = record.value();
        K key = record.key();
        int partition = record.partition();
        long offset = record.offset();

        log.info("Filtering record with key: {}, partition: {}, offset: {}", key, partition, offset);

        return consumers.stream()
                .filter(c -> c.supports(headers, value, key, partition, offset))
                .findFirst()
                .map(c -> c.discard(headers, value, key, partition, offset))
                .orElse(false);
    }
}