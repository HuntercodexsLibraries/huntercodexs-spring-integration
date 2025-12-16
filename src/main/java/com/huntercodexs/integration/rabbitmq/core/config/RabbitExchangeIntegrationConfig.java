package com.huntercodexs.integration.rabbitmq.core.config;

import com.huntercodexs.integration.rabbitmq.core.dto.RabbitDefaultIntegrationDto;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitConsumersIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalIntegrationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil.defaultConsumerPropertiesCheck;
import static com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil.doLog;

@Configuration
@RequiredArgsConstructor
public class RabbitExchangeIntegrationConfig {

    private final RabbitGlobalIntegrationProperties globalProperties;
    private final RabbitConsumersIntegrationProperties consumersProperties;

    @Bean
    public Declarables rabbitDeclarables() {
        List<Declarable> declarableList = new ArrayList<>();

        if (consumersProperties.getConsumers() == null) return new Declarables(declarableList);

        if (defaultCheck(true)) {
            List<RabbitConsumersIntegrationProperties> consumer = new ArrayList<>();
            consumer.add(consumersProperties);
            consumersProperties.setConsumers(consumer);
        }

        for (RabbitConsumersIntegrationProperties cfg : consumersProperties.getConsumers()) {

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

                callLog("Retry queues created successfully {}.retry", queueName, true);
            }

            // DLQ
            if (cfg.isDlqEnabled()) {
                Exchange dlqExchange = ExchangeBuilder.directExchange(exchangeName + ".dlq").durable(true).build();
                declarableList.add(dlqExchange);

                Queue dlqQueue = QueueBuilder.durable(queueName + ".dlq").build();
                declarableList.add(dlqQueue);

                Binding dlqBinding = BindingBuilder.bind(dlqQueue).to((DirectExchange) dlqExchange).with(routingKey + ".dlq");
                declarableList.add(dlqBinding);

                callLog("DLQ queues created successfully {}.dlq", queueName, true);
            }
        }

        return new Declarables(declarableList);
    }

    @Bean
    public String[] dynamicRabbitQueues() {

        if (defaultCheck(false)) {
            List<RabbitConsumersIntegrationProperties> consumer = new ArrayList<>();
            consumer.add(consumersProperties);
            consumersProperties.setConsumers(consumer);
        }

        if (consumersProperties.getConsumers() == null || consumersProperties.getConsumers().isEmpty()) {
            callLog("Consumers not found for creating queue", null, false);
            return new String[0];
        }

        String[] queues = consumersProperties.getConsumers().stream()
                .map(RabbitConsumersIntegrationProperties::getQueue)
                .toArray(String[]::new);

        callLog("Dynamic queues loaded successfully {}", (Object) queues, true);

        return queues;
    }

    private Queue buildMainQueue(RabbitConsumersIntegrationProperties cfg, String queueName, String exchangeName, String routingKey) {

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

            callLog("Retry is enabled without separate DLQ, route dead-letter to retry exchange", null, false);
        }

        Queue mainQueue = mainQueueBuilder.build();
        callLog("Main Queue created successfully {}", mainQueue.getName(), false);
        return mainQueue;
    }

    private Exchange buildExchange(String type, String name) {
        if (type == null) type = "direct"; //default
        Exchange exchange = switch (type.toLowerCase()) {
            case "topic" -> ExchangeBuilder.topicExchange(name).durable(true).build();
            case "fanout" -> ExchangeBuilder.fanoutExchange(name).durable(true).build();
            case "headers" -> ExchangeBuilder.headersExchange(name).durable(true).build();
            default -> ExchangeBuilder.directExchange(name).durable(true).build();
        };

        callLog("Exchange created successfully {}", exchange.getName(), false);

        return exchange;
    }

    private Binding buildBinding(Queue queue, Exchange exchange, String type, String routingKey) {
        Binding binding;
        if ("fanout".equalsIgnoreCase(type)) {
            binding = BindingBuilder.bind(queue).to((FanoutExchange) exchange);
        } else if ("topic".equalsIgnoreCase(type)) {
            binding = BindingBuilder.bind(queue).to((TopicExchange) exchange).with(routingKey);
        } else {
            binding = BindingBuilder.bind(queue).to((DirectExchange) exchange).with(routingKey);
        }

        callLog("Bindings created successfully {}", binding.getDestination(), false);

        return binding;
    }

    private boolean defaultCheck(boolean logOver) {
        RabbitDefaultIntegrationDto defaultIntegrationDto = new RabbitDefaultIntegrationDto();
        defaultIntegrationDto.setName(consumersProperties.getName());
        defaultIntegrationDto.setExchange(consumersProperties.getExchange());
        defaultIntegrationDto.setRoutingKey(consumersProperties.getRoutingKey());
        if (!logOver) {
            defaultIntegrationDto.setLogEnabled(false);
        } else {
            defaultIntegrationDto.setLogEnabled(consumersProperties.isLogEnabled() || globalProperties.isLogEnabled());
        }
        return defaultConsumerPropertiesCheck(defaultIntegrationDto);
    }

    private void callLog(String text, Object args, boolean logOver) {
        RabbitDefaultIntegrationDto defaultIntegrationDto = new RabbitDefaultIntegrationDto();
        if (!logOver) {
            defaultIntegrationDto.setLogEnabled(false);
        } else {
            defaultIntegrationDto.setLogEnabled(consumersProperties.isLogEnabled() || globalProperties.isLogEnabled());
        }
        defaultIntegrationDto.setLogText(text);
        defaultIntegrationDto.setLogArgs(args);
        doLog(defaultIntegrationDto);
    }

}
