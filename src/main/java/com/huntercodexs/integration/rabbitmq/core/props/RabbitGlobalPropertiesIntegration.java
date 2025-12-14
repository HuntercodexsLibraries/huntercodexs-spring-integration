package com.huntercodexs.integration.rabbitmq.core.props;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

@Data
@Service
@Configuration
@ConfigurationProperties(prefix = GLOBAL_BASE_CONFIG+".rabbitmq.global")
public class RabbitGlobalPropertiesIntegration {

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.logEnabled:false}")
    private boolean logEnabled;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.ackMode:MANUAL}")
    private String ackMode;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.concurrentConsumers:1}")
    private int concurrentConsumers;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.maxConcurrentConsumers:5}")
    private int maxConcurrentConsumers;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.prefetch:10}")
    private int prefetch;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.initialInterval:1000}")
    private long initialInterval;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.maxInterval:10000}")
    private long maxInterval;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.multiplier:1.5}")
    private double multiplier;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.maxAttempts:3}")
    private int maxAttempts;

}

