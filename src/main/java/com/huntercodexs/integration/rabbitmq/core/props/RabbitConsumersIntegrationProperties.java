package com.huntercodexs.integration.rabbitmq.core.props;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

@Data
@Service
@Configuration
@ConfigurationProperties(prefix = GLOBAL_BASE_CONFIG+".rabbitmq")
public class RabbitConsumersIntegrationProperties {

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.name:}")
    private String name;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.exchange:}")
    private String exchange;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.routingKey:}")
    private String routingKey;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.queue:}")
    private String queue;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.exchangeType:direct}")
    private String exchangeType;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.deliveryMode:persistent}")
    private String deliveryMode;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.retryTtlMilliseconds:5000}")
    private Integer retryTtlMilliseconds;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.maxRetries:3}")
    private Integer maxRetries;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.retryEnabled:false}")
    private boolean retryEnabled;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.dlqEnabled:false}")
    private boolean dlqEnabled;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.consumers.logEnabled:false}")
    private boolean logEnabled;

    private List<RabbitConsumersIntegrationProperties> consumers;

}

