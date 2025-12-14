package com.huntercodexs.integration.rabbitmq.consumer;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitConsumersPropertiesIntegration;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DynamicRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(DynamicRabbitListener.class);

    private final StrategyRegistry registry;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitConsumersPropertiesIntegration consumersProperties;

    @RabbitListener(queues = "#{dynamicRabbitQueues}", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {

        String raw = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        String strategyName = (String) headers.get("strategy");

        // Ack tag for later
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // Prevent ambiguous usage
        if (checkGlobalConsumerPropertiesSet()) {
            List<RabbitConsumersPropertiesIntegration> consumer = new ArrayList<>();
            consumer.add(consumersProperties);
            consumersProperties.setConsumers(consumer);
        }

        // Resolve configuration
        RabbitConsumersPropertiesIntegration cfg = consumersProperties.getConsumers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(strategyName))
                .findFirst()
                .orElse(null);

        // Strategy discover
        RabbitConsumerStrategy strategy = registry.getByStrategyName(strategyName);

        if (strategy == null) {
            log.warn("Strategy not found -> send to DLQ if configured, ack to drop from queue");

            if (cfg != null && cfg.isDlqEnabled()) {
                finalize(cfg.getExchange() + ".dlq", cfg.getRoutingKey() + ".dlq", strategyName, raw, -1);
            }

            channel.basicAck(deliveryTag, false);
            return;
        }

        try {

            strategy.messageConsumer(raw, message, headers);
            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {

            int nextRetry = currentRetry(headers) + 1;

            if (cfg != null && cfg.isRetryEnabled() && nextRetry <= cfg.getMaxRetries()) {
                sendToRetry(cfg, strategyName, raw, nextRetry, deliveryTag, channel);
            } else {
                sentToDlq(cfg, strategyName, raw, deliveryTag, channel);
            }
        }
    }

    private void sendToRetry(
            RabbitConsumersPropertiesIntegration cfg,
            String strategyName,
            String raw,
            int nextRetry,
            long deliveryTag,
            Channel channel
    ) throws Exception {
        // Send to retry exchange
        finalize(cfg.getExchange() + ".retry", cfg.getRoutingKey() + ".retry", strategyName, raw, nextRetry);
        // Ack original to remove from queue
        channel.basicAck(deliveryTag, false);
    }

    private void sentToDlq(
            RabbitConsumersPropertiesIntegration cfg,
            String strategyName,
            String raw,
            long deliveryTag,
            Channel channel
    ) throws Exception {

        if (cfg != null && cfg.isDlqEnabled()) {
            // Send to DLQ (if configured) or ack/nack to drop
            finalize(cfg.getExchange() + ".dlq", cfg.getRoutingKey() + ".dlq", strategyName, raw, -1);
            // Ack original to remove from queue
            channel.basicAck(deliveryTag, false);
        } else {
            // Nack and drop (requeue = false)
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void finalize(String exchange, String routingKey, String strategyName, String raw, int nextRetry) {
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                raw,
                msg -> {
                    msg.getMessageProperties().setHeader("strategy", strategyName);
                    if (nextRetry != -1) {
                        msg.getMessageProperties().setHeader("x-retry", nextRetry);
                    }
                    return msg;
                });
    }

    private Integer currentRetry(Map<String, Object> headers) {
        int currentRetry = 0;
        Object rawRetry = headers.get("x-retry");

        if (rawRetry instanceof Integer) {
            currentRetry = (Integer) rawRetry;

        } else if (rawRetry instanceof String) {
            try {
                currentRetry = Integer.parseInt((String) rawRetry);
            } catch (Exception ignored) {
            }
        }

        return currentRetry;
    }

    private boolean checkGlobalConsumerPropertiesSet() {
        return (!consumersProperties.getName().isEmpty()) && !consumersProperties.getExchange().isEmpty() && !consumersProperties.getRoutingKey().isEmpty();
    }

}
