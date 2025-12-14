package com.huntercodexs.integration.rabbitmq.producer;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalPropertiesIntegration;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitProducersPropertiesIntegration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class RabbitProducerIntegrationImpl implements RabbitProducerIntegration {

    private static final Logger log = LoggerFactory.getLogger(RabbitProducerIntegrationImpl.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitGlobalPropertiesIntegration globalProperties;
    private final RabbitProducersPropertiesIntegration producersProperties;

    @Override
    public void send(String strategyName, Object payload) {
        send(strategyName, payload, Map.of());
    }

    @Override
    public void send(String strategyName, Object payload, Map<String, Object> extraHeaders) {

        RabbitProducersPropertiesIntegration producersProps = findProducersProperties(strategyName);

        if (globalProperties.isLogEnabled() || producersProps.isLogEnabled()) {
            log.info("Sending message to exchange '{}' with routing key '{}' for strategy '{}'",
                    producersProps.getExchange(), producersProps.getRoutingKey(), strategyName);
        }

        MessageDeliveryMode deliveryMode;

        if (producersProps.getDeliveryMode().equalsIgnoreCase("NON_PERSISTENT")) {
            deliveryMode = MessageDeliveryMode.NON_PERSISTENT;
        } else {
            deliveryMode = MessageDeliveryMode.PERSISTENT;
        }

        rabbitTemplate.convertAndSend(
                producersProps.getExchange(),
                producersProps.getRoutingKey(),
                payload,
                msg -> {
                    msg.getMessageProperties().setHeader("strategy", strategyName);
                    msg.getMessageProperties().setHeader("messageId", UUID.randomUUID().toString());
                    extraHeaders.forEach((k, v) -> msg.getMessageProperties().setHeader(k, v));
                    msg.getMessageProperties().setDeliveryMode(deliveryMode);
                    return msg;
                }
        );
    }

    private RabbitProducersPropertiesIntegration findProducersProperties(String strategyName) {
        Optional<RabbitProducersPropertiesIntegration> cfgOpt = producersProperties.getProducers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(strategyName))
                .findFirst();

        if (
                (!producersProperties.getName().isEmpty() && producersProperties.getName().equalsIgnoreCase(strategyName))
                && !producersProperties.getExchange().isEmpty()
                && !producersProperties.getRoutingKey().isEmpty()
        ) {
            log.warn("Using default producer configuration for strategy: {}", strategyName);
            producersProperties.setProducers(null);
            return producersProperties;
        }

        return cfgOpt.orElseThrow(() -> new IllegalArgumentException("Configuration not found for producer strategy: " + strategyName));
    }
}
