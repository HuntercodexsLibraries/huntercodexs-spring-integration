package com.huntercodexs.integration.kafka.consumer.filter;

import com.huntercodexs.integration.kafka.consumer.process.KafkaConsumerIntegrationProcess;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaConsumerIntegrationFilter<K, V> implements RecordFilterStrategy<K, V> {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerIntegrationFilter.class);

    private final List<KafkaConsumerIntegrationProcess> consumers;

    @Override
    public boolean filter(ConsumerRecord<K, V> consumerRecord) {
        Headers headers = consumerRecord.headers();
        V value = consumerRecord.value();
        K key = consumerRecord.key();
        int partition = consumerRecord.partition();
        long offset = consumerRecord.offset();

        log.info("Filtering record with key: {}, partition: {}, offset: {}", key, partition, offset);

        KafkaConsumerIntegrationProcess strategy = consumers.stream()
                .filter(c -> c.supports(headers, value, key, partition, offset))
                .findFirst()
                .orElse(null);

        if (strategy == null) return true;

        log.info("Using strategy: {} for record with key: {}, partition: {}, offset: {}", strategy.getClass().getSimpleName(), key, partition, offset);

        return strategy.discard(headers, value, key, partition, offset);
    }
}