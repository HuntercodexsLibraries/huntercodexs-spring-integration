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
@ConfigurationProperties(prefix = GLOBAL_BASE_CONFIG+".rabbitmq.producers")
public class RabbitProducersPropertiesIntegration {

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.name}")
    private String name;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.exchange}")
    private String exchange;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.routingKey}")
    private String routingKey;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.deliveryMode:persistent}")
    private String deliveryMode;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.exchangeType:direct}")
    private String exchangeType;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.retryTtlMilliseconds:5000}")
    private Integer retryTtlMilliseconds;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.maxRetries:3}")
    private Integer maxRetries;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.retryEnabled:false}")
    private boolean retryEnabled;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.dlqEnabled:false}")
    private boolean dlqEnabled;

    @Value("${"+ GLOBAL_BASE_CONFIG +".rabbitmq.producers.logEnabled:false}")
    private boolean logEnabled;

    private List<RabbitProducersPropertiesIntegration> producers;

}

