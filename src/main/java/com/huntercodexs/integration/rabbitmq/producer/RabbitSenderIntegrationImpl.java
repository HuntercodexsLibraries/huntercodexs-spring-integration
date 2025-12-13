package com.huntercodexs.integration.rabbitmq.producer;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitPropertiesIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class RabbitSenderIntegrationImpl implements RabbitSenderIntegration {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitPropertiesIntegration properties;

    @Override
    public void send(String strategyName, Object payload) {
        send(strategyName, payload, Map.of());
    }

    @Override
    public void send(String strategyName, Object payload, Map<String, Object> extraHeaders) {

        RabbitPropertiesIntegration.RabbitConsumerConfig cfg = findProperties(strategyName);

        rabbitTemplate.convertAndSend(
                cfg.getExchange(),
                cfg.getRoutingKey(),
                payload,
                msg -> {
                    msg.getMessageProperties().setHeader("strategy", strategyName);
                    msg.getMessageProperties().setHeader("messageId", UUID.randomUUID().toString());
                    extraHeaders.forEach((k, v) -> msg.getMessageProperties().setHeader(k, v));
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT); // persistent by default
                    return msg;
                }
        );
    }

    private RabbitPropertiesIntegration.RabbitConsumerConfig findProperties(String strategyName) {
        Optional<RabbitPropertiesIntegration.RabbitConsumerConfig> cfgOpt = properties.getConsumers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(strategyName))
                .findFirst();
        return cfgOpt.orElseThrow(() -> new IllegalArgumentException("No config for strategy: " + strategyName));
    }
}
