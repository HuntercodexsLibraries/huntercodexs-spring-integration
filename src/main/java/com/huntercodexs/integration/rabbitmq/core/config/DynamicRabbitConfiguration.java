package com.huntercodexs.integration.rabbitmq.core.config;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitConsumersPropertiesIntegration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DynamicRabbitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DynamicRabbitConfiguration.class);

    private final RabbitConsumersPropertiesIntegration consumersProperties;

    @Bean
    public Declarables rabbitDeclarables() {
        List<Declarable> declarableList = new ArrayList<>();

        if (consumersProperties.getConsumers() == null) return new Declarables(declarableList);

        if (checkGlobalConsumerPropertiesSet()) {
            log.warn("Single RabbitMQ consumer properties detected (name, exchange, routingKey), " +
                    "overriding 'consumers' list with a single entry. Is it intended?");
            List<RabbitConsumersPropertiesIntegration> consumer = new ArrayList<>();
            consumer.add(consumersProperties);
            consumersProperties.setConsumers(consumer);
        }

        for (RabbitConsumersPropertiesIntegration cfg : consumersProperties.getConsumers()) {

            String exchangeName = cfg.getExchange();
            String queueName = cfg.getQueue();
            String routingKey = cfg.getRoutingKey();

            Exchange mainExchange = buildExchange(String.valueOf(cfg.getExchangeType()).toLowerCase(), exchangeName);
            declarableList.add(mainExchange);

            Queue mainQueue = buildMainQueue(cfg, queueName, exchangeName, routingKey);
            declarableList.add(mainQueue);

            // Main binding (handle topic/fanout differences)
            Binding mainBinding = buildBinding(mainQueue, mainExchange, String.valueOf(cfg.getExchangeType()).toLowerCase(), routingKey);
            declarableList.add(mainBinding);

            // Retry exchange & queue
            if (cfg.isRetryEnabled() && cfg.getRetryTtlMilliseconds() != null && cfg.getRetryTtlMilliseconds() > 0) {
                Exchange retryExchange = ExchangeBuilder.directExchange(exchangeName + ".retry").durable(true).build();
                declarableList.add(retryExchange);

                Queue retryQueue = QueueBuilder.durable(queueName + ".retry")
                        // after TTL expires, send back to main exchange (dead-letter-exchange)
                        .withArgument("x-dead-letter-exchange", exchangeName)
                        .withArgument("x-dead-letter-routing-key", routingKey)
                        .withArgument("x-message-ttl", cfg.getRetryTtlMilliseconds())
                        .build();
                declarableList.add(retryQueue);

                Binding retryBinding = BindingBuilder.bind(retryQueue).to((DirectExchange) retryExchange).with(routingKey + ".retry");

                declarableList.add(retryBinding);
            }

            // DLQ
            if (cfg.isDlqEnabled()) {
                Exchange dlqExchange = ExchangeBuilder.directExchange(exchangeName + ".dlq").durable(true).build();
                declarableList.add(dlqExchange);

                Queue dlqQueue = QueueBuilder.durable(queueName + ".dlq").build();
                declarableList.add(dlqQueue);

                Binding dlqBinding = BindingBuilder.bind(dlqQueue).to((DirectExchange) dlqExchange).with(routingKey + ".dlq");
                declarableList.add(dlqBinding);
            }
        }

        return new Declarables(declarableList);
    }

    @Bean
    public String[] dynamicRabbitQueues() {

        if (checkGlobalConsumerPropertiesSet()) {
            List<RabbitConsumersPropertiesIntegration> consumer = new ArrayList<>();
            consumer.add(consumersProperties);
            consumersProperties.setConsumers(consumer);
        }

        if (consumersProperties.getConsumers() == null || consumersProperties.getConsumers().isEmpty()) {
            log.warn("Consumers not found for creating queue");
            return new String[0];
        }

        return consumersProperties.getConsumers().stream()
                .map(RabbitConsumersPropertiesIntegration::getQueue)
                .toArray(String[]::new);
    }

    private Queue buildMainQueue(RabbitConsumersPropertiesIntegration cfg, String queueName, String exchangeName, String routingKey) {

        QueueBuilder mainQueueBuilder = QueueBuilder.durable(queueName);

        if (cfg.isDlqEnabled() && cfg.isRetryEnabled()) {

            mainQueueBuilder
                    .withArgument("x-dead-letter-exchange", exchangeName + ".dlq")
                    .withArgument("x-dead-letter-routing-key", routingKey + ".dlq")
                    .withArgument("x-dead-letter-exchange", exchangeName + ".retry")
                    .withArgument("x-dead-letter-routing-key", routingKey + ".retry");

        } else if (cfg.isDlqEnabled()) {

            mainQueueBuilder
                    .withArgument("x-dead-letter-exchange", exchangeName + ".dlq")
                    .withArgument("x-dead-letter-routing-key", routingKey + ".dlq");

        } else if (cfg.isRetryEnabled() && cfg.getRetryTtlMilliseconds() != null && cfg.getRetryTtlMilliseconds() > 0) {

            mainQueueBuilder
                    .withArgument("x-dead-letter-exchange", exchangeName + ".retry")
                    .withArgument("x-dead-letter-routing-key", routingKey + ".retry");

            log.warn("Retry is enabled without separate DLQ, route dead-letter to retry exchange");
        }

        Queue mainQueue = mainQueueBuilder.build();
        log.info("Main Queue created successfully {}", mainQueue.getName());
        return mainQueue;
    }

    private Exchange buildExchange(String type, String name) {
        if (type == null) type = "direct"; //default
        return switch (type.toLowerCase()) {
            case "topic" -> ExchangeBuilder.topicExchange(name).durable(true).build();
            case "fanout" -> ExchangeBuilder.fanoutExchange(name).durable(true).build();
            case "headers" -> ExchangeBuilder.headersExchange(name).durable(true).build();
            default -> ExchangeBuilder.directExchange(name).durable(true).build();
        };
    }

    private Binding buildBinding(Queue queue, Exchange exchange, String type, String routingKey) {
        if ("fanout".equalsIgnoreCase(type)) {
            return BindingBuilder.bind(queue).to((FanoutExchange) exchange);
        } else if ("topic".equalsIgnoreCase(type)) {
            return BindingBuilder.bind(queue).to((TopicExchange) exchange).with(routingKey);
        } else {
            return BindingBuilder.bind(queue).to((DirectExchange) exchange).with(routingKey);
        }
    }

    private boolean checkGlobalConsumerPropertiesSet() {
        return (!consumersProperties.getName().isEmpty()) && !consumersProperties.getExchange().isEmpty() && !consumersProperties.getRoutingKey().isEmpty();
    }

}
