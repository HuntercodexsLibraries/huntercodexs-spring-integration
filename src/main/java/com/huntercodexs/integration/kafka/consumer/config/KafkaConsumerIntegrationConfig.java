package com.huntercodexs.integration.kafka.consumer.config;

import com.huntercodexs.integration.kafka.consumer.filter.KafkaConsumerIntegrationFilter;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL;

@EnableKafka
@Configuration
public class KafkaConsumerIntegrationConfig extends KafkaConsumerCommonIntegrationConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerIntegrationConfig.class);

    @Autowired
    private KafkaConsumerIntegrationFilter<String, String> filter;

    @PostConstruct
    public void kafkaConsumerStarted() {
        if (!kafkaConsumerEnabled) {
            log.warn("Kafka integration is disabled. Consumers will not be initialized.");
            return;
        }
        log.info("Kafka Consumers initialized successfully!");
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        if (!kafkaConsumerEnabled) {
            log.warn("Kafka integration is disabled. ConsumerFactory will not be created.");
            return null;
        }

        try {
            Map<String, Object> props = new HashMap<>(commonKafkaProps());
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapConsumerServer);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetType);
            return new DefaultKafkaConsumerFactory<>(props);
        } catch (Exception ex) {
            log.error("Error creating Kafka ConsumerFactory: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        if (!kafkaConsumerEnabled) {
            log.warn("Kafka integration is disabled. KafkaListenerContainerFactory for consumer will not be created.");
            return null;
        }

        try {
            ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setAckDiscarded(discardAckMode);
            factory.getContainerProperties().setAckMode(ackMode(ackMode));
            factory.setConsumerFactory(consumerFactory());
            factory.setRecordFilterStrategy(filter);


            log.info("Starting Kafka Listener Container Factory for Consumer: {}", factory.getConsumerFactory().getConfigurationProperties());

            return factory;
        } catch (Exception ex) {
            log.error("Error creating Kafka Listener Container Factory: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    private ContainerProperties.AckMode ackMode(String mode) {
        if (mode == null || mode.isBlank() || mode.equalsIgnoreCase("MANUAL")) {
            return MANUAL;
        } else if (mode.equalsIgnoreCase("RECORD")) {
            return ContainerProperties.AckMode.RECORD;
        } else if (mode.equalsIgnoreCase("BATCH")) {
            return ContainerProperties.AckMode.BATCH;
        } else if (mode.equalsIgnoreCase("TIME")) {
            return ContainerProperties.AckMode.TIME;
        } else if (mode.equalsIgnoreCase("COUNT")) {
            return ContainerProperties.AckMode.COUNT;
        } else if (mode.equalsIgnoreCase("COUNT_TIME")) {
            return ContainerProperties.AckMode.COUNT_TIME;
        } else {
            log.warn("Invalid ack mode configured: {}. Defaulting to MANUAL.", mode);
            return MANUAL;
        }
    }
}
