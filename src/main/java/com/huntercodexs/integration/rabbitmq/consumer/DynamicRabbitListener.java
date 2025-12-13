package com.huntercodexs.integration.rabbitmq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitPropertiesIntegration;
import com.huntercodexs.integration.rabbitmq.consumer.implement.RabbitConsumerStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DynamicRabbitListener {

    private final StrategyRegistry registry;
    private final RabbitPropertiesIntegration properties;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "#{dynamicRabbitQueues}", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {
        String raw = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String,Object> headers = message.getMessageProperties().getHeaders();
        String strategyName = (String) headers.get("strategy");

        // ack tag for later
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // resolve cfg
        RabbitPropertiesIntegration.RabbitConsumerConfig cfg = properties.getConsumers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(strategyName))
                .findFirst()
                .orElse(null);

        RabbitConsumerStrategy strategy = registry.getByName(strategyName);

        if (strategy == null) {
            // no strategy found -> send to DLQ if configured, ack to drop from queue
            if (cfg != null && Boolean.TRUE.equals(cfg.getDlqEnabled())) {
                rabbitTemplate.convertAndSend(cfg.getExchange() + ".dlq", cfg.getRoutingKey() + ".dlq", raw, m -> {
                    m.getMessageProperties().setHeader("strategy", strategyName);
                    return m;
                });
            }
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            // delegate raw string — strategy can parse to POJO if needed
            strategy.handle(raw, message, headers);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            // determine retry count
            Integer currentRetry = 0;
            Object rawRetry = headers.get("x-retry");
            if (rawRetry instanceof Integer) currentRetry = (Integer) rawRetry;
            else if (rawRetry instanceof String) {
                try { currentRetry = Integer.parseInt((String) rawRetry); } catch (Exception ignored) {}
            }

            int nextRetry = currentRetry + 1;
            if (cfg != null && Boolean.TRUE.equals(cfg.getRetryEnabled()) && nextRetry <= (cfg.getMaxRetries() == null ? 3 : cfg.getMaxRetries())) {
                // send to retry exchange
                rabbitTemplate.convertAndSend(cfg.getExchange() + ".retry", cfg.getRoutingKey() + ".retry", raw, m -> {
                    m.getMessageProperties().setHeader("strategy", strategyName);
                    m.getMessageProperties().setHeader("x-retry", nextRetry);
                    return m;
                });
                // ack original to remove from queue
                channel.basicAck(deliveryTag, false);
            } else {
                // send to DLQ (if configured) or ack/nack to drop
                if (cfg != null && Boolean.TRUE.equals(cfg.getDlqEnabled())) {
                    rabbitTemplate.convertAndSend(cfg.getExchange() + ".dlq", cfg.getRoutingKey() + ".dlq", raw, m -> {
                        m.getMessageProperties().setHeader("strategy", strategyName);
                        m.getMessageProperties().setHeader("x-retry", nextRetry);
                        return m;
                    });
                    channel.basicAck(deliveryTag, false);
                } else {
                    // nack and drop (requeue = false)
                    channel.basicNack(deliveryTag, false, false);
                }
            }
        }
    }
}
