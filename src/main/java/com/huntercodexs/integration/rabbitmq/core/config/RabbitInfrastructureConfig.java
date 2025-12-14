package com.huntercodexs.integration.rabbitmq.core.config;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalPropertiesIntegration;
import com.huntercodexs.integration.rabbitmq.core.retry.RabbitRetryLoggingListener;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@RequiredArgsConstructor
public class RabbitInfrastructureConfig {

    private final RabbitGlobalPropertiesIntegration globalProperties;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RabbitGlobalPropertiesIntegration globalProperties
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.valueOf(globalProperties.getAckMode()));
        factory.setConcurrentConsumers(globalProperties.getConcurrentConsumers());
        factory.setMaxConcurrentConsumers(globalProperties.getMaxConcurrentConsumers());
        factory.setPrefetchCount(globalProperties.getPrefetch());
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rt = new RabbitTemplate(connectionFactory);
        rt.setMessageConverter(jackson2JsonMessageConverter());
        rt.setRetryTemplate(rabbitRetryTemplate());
        return rt;
    }

    @Bean
    public RetryTemplate rabbitRetryTemplate() {
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(globalProperties.getInitialInterval());
        backOff.setMaxInterval(globalProperties.getMaxInterval());
        backOff.setMultiplier(globalProperties.getMultiplier());

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(globalProperties.getMaxAttempts());

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOff);
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.registerListener(new RabbitRetryLoggingListener(globalProperties.isLogEnabled()));

        return retryTemplate;
    }
}

