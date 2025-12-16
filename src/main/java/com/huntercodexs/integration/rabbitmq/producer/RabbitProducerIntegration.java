package com.huntercodexs.integration.rabbitmq.producer;

import com.huntercodexs.integration.rabbitmq.core.dto.RabbitDefaultIntegrationDto;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitProducersIntegrationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil.doLog;
import static com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil.stripPayload;

@Service
@RequiredArgsConstructor
public final class RabbitProducerIntegration {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitGlobalIntegrationProperties globalProperties;
    private final RabbitProducersIntegrationProperties producersProperties;

    public void send(String strategyName, Object payload) {
        send(strategyName, payload, Map.of());
    }

    public void send(String strategyName, Object payload, Map<String, Object> extraHeaders) {

        RabbitProducersIntegrationProperties producersProps = findProducersProperties(strategyName);

        callLog("Sending message to exchange {}", producersProps.getExchange());

        MessageDeliveryMode deliveryMode;

        if (producersProps.getDeliveryMode().equalsIgnoreCase("NON_PERSISTENT")) {
            deliveryMode = MessageDeliveryMode.NON_PERSISTENT;
        } else {
            deliveryMode = MessageDeliveryMode.PERSISTENT;
        }

        rabbitTemplate.convertAndSend(
                producersProps.getExchange(),
                producersProps.getRoutingKey(),
                stripPayload(String.valueOf(payload)),
                msg -> {
                    msg.getMessageProperties().setHeader("strategy", strategyName);
                    msg.getMessageProperties().setHeader("messageId", UUID.randomUUID().toString());
                    extraHeaders.forEach((k, v) -> msg.getMessageProperties().setHeader(k, v));
                    msg.getMessageProperties().setDeliveryMode(deliveryMode);
                    return msg;
                }
        );
    }

    private RabbitProducersIntegrationProperties findProducersProperties(String strategyName) {
        callLog("Trying to find producers properties for strategy {}", strategyName);

        Optional<RabbitProducersIntegrationProperties> cfgOpt = producersProperties.getProducers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(strategyName))
                .findFirst();

        if (
                (!producersProperties.getName().isEmpty() && producersProperties.getName().equalsIgnoreCase(strategyName))
                && !producersProperties.getExchange().isEmpty()
                && !producersProperties.getRoutingKey().isEmpty()
        ) {
            callLog("Using default producer configuration for strategy: {}", strategyName);
            return producersProperties;
        }

        return cfgOpt.orElseThrow(() -> new IllegalArgumentException("Configuration not found for producer strategy: " + strategyName));
    }

    private void callLog(String text, Object args) {
        RabbitDefaultIntegrationDto defaultIntegrationDto = new RabbitDefaultIntegrationDto();
        defaultIntegrationDto.setLogEnabled(producersProperties.isLogEnabled() || globalProperties.isLogEnabled());
        defaultIntegrationDto.setLogText(text);
        defaultIntegrationDto.setLogArgs(args);
        doLog(defaultIntegrationDto);
    }
}
