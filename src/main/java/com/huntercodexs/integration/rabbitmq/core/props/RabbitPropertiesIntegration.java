package com.huntercodexs.integration.rabbitmq.core.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

@Data
@Configuration
@ConfigurationProperties(prefix = GLOBAL_BASE_CONFIG+".rabbitmq")
public class RabbitPropertiesIntegration {

    private List<RabbitConsumerConfig> consumers;
    private Listener listener = new Listener();

    @Data
    public static class RabbitConsumerConfig {
        private String name;
        private String queue;
        private String exchange;
        private String routingKey;
        private String exchangeType = "direct"; // use: direct, topic, fanout, headers
        private Boolean retryEnabled = false;
        private Integer retryTtl; // use: milliseconds
        private Integer maxRetries = 3;
        private Boolean dlqEnabled = false;
        private String payloadClass;
        private String deadLetterExchange;
        private String deadLetterRoutingKey;
        private Boolean durable = true;
        private Boolean exclusive = false;
        private Boolean autoDelete = false;
        private String description;
        private String owner;
        private String version;
        private String createdAt;
        private String updatedAt;
        private String notes;
        private String additionalInfo;
        private String contact;
        private String environment;
        private String project;
        private String team;
        private String module;
        private String service;
        private String application;
        private String virtualHost;
        private String ackMode;
    }

    @Data
    public static class Listener {
        private int concurrentConsumers = 1;
        private int maxConcurrentConsumers = 5;
        private int prefetch = 10;
        private boolean defaultRequeueRejected = false;
    }
}

