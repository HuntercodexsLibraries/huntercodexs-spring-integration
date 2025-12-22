package com.huntercodexs.integration.rabbitmq.consumer;

import com.huntercodexs.integration.rabbitmq.core.dto.RabbitDefaultIntegrationDto;
import com.huntercodexs.integration.rabbitmq.core.handler.RabbitExceptionDlqIntegration;
import com.huntercodexs.integration.rabbitmq.core.handler.RabbitExceptionRetryIntegration;
import com.huntercodexs.integration.rabbitmq.core.handler.RabbitExceptionRouterIntegration;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitConsumersIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalIntegrationProperties;
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

import static com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil.*;

@Component
@RequiredArgsConstructor
public class RabbitConsumerIntegration {

    private static final Logger log = LoggerFactory.getLogger(RabbitConsumerIntegration.class);

    private final StrategyRegistry registry;
    private final RabbitTemplate rabbitTemplate;

    private final RabbitGlobalIntegrationProperties globalProperties;
    private final RabbitConsumersIntegrationProperties consumersProperties;

    @RabbitListener(queues = "#{dynamicRabbitQueues}", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {

        String raw = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        String strategyName = (String) headers.get("strategy");

        // Ack tag for later
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // Prevent ambiguous usage
        if (defaultCheck(false)) {
            List<RabbitConsumersIntegrationProperties> consumer = new ArrayList<>();
            consumer.add(consumersProperties);
            consumersProperties.setConsumers(consumer);
        }

        // Resolve configuration
        RabbitConsumersIntegrationProperties cfg = consumersProperties.getConsumers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(strategyName))
                .findFirst()
                .orElse(null);

        // Strategy discover
        RabbitConsumerStrategy strategy = registry.getByStrategyName(strategyName);

        if (strategy == null) {

            callLog("Strategy not found -> send to DLQ if configured, ack to drop from queue", null);

            if (cfg != null && cfg.isDlqEnabled()) {
                sendTo(cfg.getExchange() + ".dlq", cfg.getRoutingKey() + ".dlq", strategyName, raw, -1);
            }

            channel.basicAck(deliveryTag, false);
            return;
        }

        try {

            strategy.messageConsumer(raw, message, headers);
            channel.basicAck(deliveryTag, false);

        } catch (RabbitExceptionRetryIntegration ex) {

            int nextRetry = currentRetry(headers);

            if (cfg != null && cfg.isRetryEnabled() && nextRetry <= cfg.getMaxRetries()) {
                sendToRetry(cfg, strategyName, raw, nextRetry, deliveryTag, channel);
            } else {
                sentToDlq(cfg, strategyName, raw, deliveryTag, channel);
            }

        } catch (RabbitExceptionDlqIntegration ex) {
            sentToDlq(cfg, strategyName, raw, deliveryTag, channel);

        } catch (RabbitExceptionRouterIntegration ex) {
            routerTo(cfg, ex.getTarget(), raw, deliveryTag, channel);
        }
    }

    private void routerTo(
            RabbitConsumersIntegrationProperties cfg,
            String strategyName,
            String raw,
            long deliveryTag,
            Channel channel
    ) throws Exception {

        callLog("Sending message to router queue", null);

        if (cfg != null) {
            // Send to retry exchange
            sendTo(cfg.getExchange(), cfg.getRoutingKey(), strategyName, raw, -1);
            // Ack original to remove from queue
            channel.basicAck(deliveryTag, false);
        } else {
            callLog("[CRITICAL] Missing Router Configuration", null);
        }
    }

    private void sendToRetry(
            RabbitConsumersIntegrationProperties cfg,
            String strategyName,
            String raw,
            int nextRetry,
            long deliveryTag,
            Channel channel
    ) throws Exception {

        callLog("Sending message to retry queue", null);

        // Send to retry exchange
        sendTo(cfg.getExchange() + ".retry", cfg.getRoutingKey() + ".retry", strategyName, raw, nextRetry);
        // Ack original to remove from queue
        channel.basicAck(deliveryTag, false);
    }

    private void sentToDlq(
            RabbitConsumersIntegrationProperties cfg,
            String strategyName,
            String raw,
            long deliveryTag,
            Channel channel
    ) throws Exception {

        callLog("Sending message to DLQ queue", null);

        if (cfg != null && cfg.isDlqEnabled()) {
            // Send to DLQ (if configured) or ack/nack to drop
            sendTo(cfg.getExchange() + ".dlq", cfg.getRoutingKey() + ".dlq", strategyName, raw, -1);
            // Ack original to remove from queue
            channel.basicAck(deliveryTag, false);
        } else {
            // Nack and drop (requeue = false)
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void sendTo(String exchange, String routingKey, String strategyName, String payload, int nextRetry) {
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                stripPayload(payload),
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
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

        return currentRetry + 1;
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

    private void callLog(String text, Object args) {
        RabbitDefaultIntegrationDto defaultIntegrationDto = new RabbitDefaultIntegrationDto();
        defaultIntegrationDto.setLogEnabled(consumersProperties.isLogEnabled() || globalProperties.isLogEnabled());
        defaultIntegrationDto.setLogText(text);
        defaultIntegrationDto.setLogArgs(args);
        doLog(defaultIntegrationDto);
    }

}
