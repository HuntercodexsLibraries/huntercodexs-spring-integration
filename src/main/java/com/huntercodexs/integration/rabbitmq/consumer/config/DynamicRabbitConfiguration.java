package com.huntercodexs.integration.rabbitmq.consumer.config;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitPropertiesIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DynamicRabbitConfiguration {

    private final RabbitPropertiesIntegration properties;

    @Bean
    public Declarables rabbitDeclarables() {
        List<Declarable> declarableList = new ArrayList<>();

        if (properties.getConsumers() == null) return new Declarables(declarableList);

        for (RabbitPropertiesIntegration.RabbitConsumerConfig cfg : properties.getConsumers()) {

            String exchangeName = cfg.getExchange();
            String queueName = cfg.getQueue();
            String routingKey = cfg.getRoutingKey();

            // main exchange
            Exchange mainExchange = buildExchange(cfg.getExchangeType(), exchangeName);
            declarableList.add(mainExchange);

            // main queue: if DLQ enabled, point dead-letter-exchange to <exchange>.dlq (or .dlx)
            QueueBuilder mainQueueBuilder = QueueBuilder.durable(queueName);
            if (Boolean.TRUE.equals(cfg.getDlqEnabled())) {
                mainQueueBuilder = mainQueueBuilder
                        .withArgument("x-dead-letter-exchange", exchangeName + ".dlq")
                        .withArgument("x-dead-letter-routing-key", routingKey + ".dlq");
            } else if (cfg.getRetryTtl() != null && cfg.getRetryTtl() > 0) {
                // if retry is enabled without separate DLQ, route dead-letter to retry exchange
                mainQueueBuilder = mainQueueBuilder
                        .withArgument("x-dead-letter-exchange", exchangeName + ".retry")
                        .withArgument("x-dead-letter-routing-key", routingKey + ".retry");
            }

            Queue mainQueue = mainQueueBuilder.build();
            declarableList.add(mainQueue);

            // main binding (handle topic/fanout differences)
            Binding mainBinding = buildBinding(mainQueue, mainExchange, cfg.getExchangeType(), routingKey);
            declarableList.add(mainBinding);

            // retry exchange & queue
            if (Boolean.TRUE.equals(cfg.getRetryEnabled()) && cfg.getRetryTtl() != null && cfg.getRetryTtl() > 0) {
                Exchange retryExchange = ExchangeBuilder.directExchange(exchangeName + ".retry").durable(true).build();
                declarableList.add(retryExchange);

                Queue retryQueue = QueueBuilder.durable(queueName + ".retry")
                        // after TTL expires, send back to main exchange (dead-letter-exchange)
                        .withArgument("x-dead-letter-exchange", exchangeName)
                        .withArgument("x-dead-letter-routing-key", routingKey)
                        .withArgument("x-message-ttl", cfg.getRetryTtl())
                        .build();
                declarableList.add(retryQueue);

                Binding retryBinding = BindingBuilder.bind(retryQueue).to((DirectExchange) retryExchange).with(routingKey + ".retry");
                declarableList.add(retryBinding);
            }

            // DLQ
            if (Boolean.TRUE.equals(cfg.getDlqEnabled())) {
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

    private Exchange buildExchange(String type, String name) {
        if (type == null) type = "direct";
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

    // helper bean to provide queue names array for listener
    @Bean
    public String[] dynamicRabbitQueues() {
        if (properties.getConsumers() == null) return new String[0];
        return properties.getConsumers().stream()
                .map(RabbitPropertiesIntegration.RabbitConsumerConfig::getQueue)
                .toArray(String[]::new);
    }
}
