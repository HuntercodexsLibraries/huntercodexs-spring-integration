package com.huntercodexs.integration.kafka.producer.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerIntegrationConfig extends KafkaProducerCommonIntegrationConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerIntegrationConfig.class);

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        if (!kafkaProducerEnabled) {
            log.warn("Kafka integration is disabled. ProducerFactory will not be created.");
            return null;
        }

        Map<String, Object> props = new HashMap<>(commonKafkaProps());
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapProducerServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        log.info("Kafka ProducerFactory configured with bootstrap servers: {}", bootstrapProducerServer);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        if (!kafkaProducerEnabled) {
            log.warn("Kafka integration is disabled. KafkaTemplate for producer will not be created.");
            return null;
        }
        return new KafkaTemplate<>(producerFactory());
    }
}
