package com.huntercodexs.integration.rabbitmq.consumer;

import com.huntercodexs.integration.rabbitmq.core.handler.RabbitExceptionDlqIntegration;
import com.huntercodexs.integration.rabbitmq.core.handler.RabbitExceptionRetryIntegration;
import com.huntercodexs.integration.rabbitmq.core.handler.RabbitExceptionRouterIntegration;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitConsumersIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitConsumerIntegrationTest {

    @Mock private StrategyRegistry registry;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private Channel channel;
    @Mock private RabbitGlobalIntegrationProperties globalProperties;

    private RabbitConsumersIntegrationProperties consumersProperties;
    private RabbitConsumerIntegration consumerIntegration;

    @BeforeEach
    void setUp() {
        consumersProperties = new RabbitConsumersIntegrationProperties();
        consumersProperties.setName("global-consumer");
        consumersProperties.setExchange("ex");
        consumersProperties.setRoutingKey("rk");
        consumersProperties.setLogEnabled(false);
        // by default no nested consumers; tests will set as needed

        consumerIntegration = new RabbitConsumerIntegration(registry, rabbitTemplate, globalProperties, consumersProperties);
    }

    private Message makeMessage(String payload, Map<String, Object> headers, long deliveryTag) {
        MessageProperties props = new MessageProperties();
        if (headers != null) {
            headers.forEach(props::setHeader);
        }
        props.setDeliveryTag(deliveryTag);
        return new Message(payload.getBytes(StandardCharsets.UTF_8), props);
    }

    @Test
    void whenStrategyNotFound_andCfgDlqEnabled_thenSendToDlq_andAck() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);
            util.when(() -> RabbitIntegrationUtil.stripPayload(anyString())).thenAnswer(inv -> inv.getArgument(0));

            String strategyName = "not-exists";
            // add a consumer config matching the strategy name
            RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
            cfg.setName(strategyName);
            cfg.setExchange("ex1");
            cfg.setRoutingKey("rk1");
            cfg.setDlqEnabled(true);
            consumersProperties.setConsumers(Collections.singletonList(cfg));

            when(registry.getByStrategyName(strategyName)).thenReturn(null);

            Message msg = makeMessage("body", Map.of("strategy", strategyName), 123L);

            consumerIntegration.onMessage(msg, channel);

            // verify DLQ send
            verify(rabbitTemplate).convertAndSend(eq("ex1.dlq"), eq("rk1.dlq"), eq("body"), any(MessagePostProcessor.class));
            verify(channel).basicAck(123L, false);
        }
    }

    @Test
    void whenStrategyExists_andProcessingSucceeds_thenAckOnly() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);

            String strategyName = "ok-strategy";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            // a config must exist for lookup but is not required for success path
            RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
            cfg.setName(strategyName);
            consumersProperties.setConsumers(List.of(cfg));

            Message msg = makeMessage("payload", Map.of("strategy", strategyName), 10L);

            consumerIntegration.onMessage(msg, channel);

            verify(strategy).messageConsumer(eq("payload"), any(Message.class), anyMap());
            verify(channel).basicAck(10L, false);
            verifyNoInteractions(rabbitTemplate);
        }
    }

    @Test
    void whenStrategyThrowsRetry_andWithinMaxRetries_thenSendToRetry_andAck() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);
            util.when(() -> RabbitIntegrationUtil.stripPayload(anyString())).thenAnswer(inv -> inv.getArgument(0));

            String strategyName = "retry-strat";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            doThrow(new RabbitExceptionRetryIntegration("test")).when(strategy).messageConsumer(anyString(), any(), anyMap());
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
            cfg.setName(strategyName);
            cfg.setExchange("ex");
            cfg.setRoutingKey("rk");
            cfg.setRetryEnabled(true);
            cfg.setMaxRetries(3);
            consumersProperties.setConsumers(List.of(cfg));

            // simulate current x-retry = 1 (so nextRetry will be 2)
            Message msg = makeMessage("payload", new HashMap<>() {{
                put("strategy", strategyName);
                put("x-retry", 1);
            }}, 55L);

            // capture MessagePostProcessor to assert headers set
            ArgumentCaptor<MessagePostProcessor> mppCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
            doNothing().when(rabbitTemplate).convertAndSend(eq("ex.retry"), eq("rk.retry"), eq("payload"), mppCaptor.capture());

            consumerIntegration.onMessage(msg, channel);

            // verify retry send and ack
            verify(rabbitTemplate).convertAndSend(eq("ex.retry"), eq("rk.retry"), eq("payload"), any(MessagePostProcessor.class));
            verify(channel).basicAck(55L, false);

            // validate MessagePostProcessor sets headers
            MessageProperties props = new MessageProperties();
            Message created = new Message("payload".getBytes(StandardCharsets.UTF_8), props);
            Message processed = mppCaptor.getValue().postProcessMessage(created);
            assertEquals(strategyName, processed.getMessageProperties().getHeaders().get("strategy"));
            assertEquals(2, processed.getMessageProperties().getHeaders().get("x-retry"));
        }
    }

    @Test
    void whenStrategyThrowsRetry_andCfgNull_thenNack() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);

            String strategyName = "no-cfg";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            doThrow(new RabbitExceptionRetryIntegration("test")).when(strategy).messageConsumer(anyString(), any(), anyMap());
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            // consumersProperties has no inner consumer list, so cfg == null
            consumersProperties.setConsumers(Collections.emptyList());

            Message msg = makeMessage("payload", Map.of("strategy", strategyName), 999L);

            consumerIntegration.onMessage(msg, channel);

            verify(channel).basicNack(999L, false, false);
        }
    }

    @Test
    void whenStrategyThrowsRetry_andExceedsMax_thenDlq_behavior_varies() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);
            util.when(() -> RabbitIntegrationUtil.stripPayload(anyString())).thenAnswer(inv -> inv.getArgument(0));

            String strategyName = "exceed-retry";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            doThrow(new RabbitExceptionRetryIntegration("test")).when(strategy).messageConsumer(anyString(), any(), anyMap());
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            // cfg that has retry enabled but maxRetries = 0 so nextRetry 1 > 0 -> go to DLQ
            RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
            cfg.setName(strategyName);
            cfg.setExchange("exdlq");
            cfg.setRoutingKey("rkdlq");
            cfg.setRetryEnabled(true);
            cfg.setMaxRetries(0);
            cfg.setDlqEnabled(true);
            consumersProperties.setConsumers(List.of(cfg));

            Message msg = makeMessage("payload", Map.of("strategy", strategyName, "x-retry", 0), 12L);

            doNothing().when(rabbitTemplate).convertAndSend(eq("exdlq.dlq"), eq("rkdlq.dlq"), eq("payload"), any(MessagePostProcessor.class));

            consumerIntegration.onMessage(msg, channel);

            verify(rabbitTemplate).convertAndSend(eq("exdlq.dlq"), eq("rkdlq.dlq"), eq("payload"), any(MessagePostProcessor.class));
            verify(channel).basicAck(12L, false);
        }
    }

    @Test
    void whenStrategyThrowsDlqIntegration_andDlqDisabled_thenNack() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);

            String strategyName = "dlq-strat";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            doThrow(new RabbitExceptionDlqIntegration("test")).when(strategy).messageConsumer(anyString(), any(), anyMap());
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
            cfg.setName(strategyName);
            cfg.setDlqEnabled(false); // disabled -> should nack
            consumersProperties.setConsumers(List.of(cfg));

            Message msg = makeMessage("body", Map.of("strategy", strategyName), 7L);

            consumerIntegration.onMessage(msg, channel);

            verify(channel).basicNack(7L, false, false);
        }
    }

    @Test
    void whenStrategyThrowsDlqIntegration_andDlqEnabled_thenSendDlq_andAck() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);
            util.when(() -> RabbitIntegrationUtil.stripPayload(anyString())).thenAnswer(inv -> inv.getArgument(0));

            String strategyName = "dlq-enabled";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            doThrow(new RabbitExceptionDlqIntegration("test")).when(strategy).messageConsumer(anyString(), any(), anyMap());
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
            cfg.setName(strategyName);
            cfg.setExchange("exd");
            cfg.setRoutingKey("rkd");
            cfg.setDlqEnabled(true);
            consumersProperties.setConsumers(List.of(cfg));

            Message msg = makeMessage("body", Map.of("strategy", strategyName), 8L);

            doNothing().when(rabbitTemplate).convertAndSend(eq("exd.dlq"), eq("rkd.dlq"), eq("body"), any(MessagePostProcessor.class));

            consumerIntegration.onMessage(msg, channel);

            verify(rabbitTemplate).convertAndSend(eq("exd.dlq"), eq("rkd.dlq"), eq("body"), any(MessagePostProcessor.class));
            verify(channel).basicAck(8L, false);
        }
    }

    @Test
    void whenStrategyThrowsRouterIntegration_andCfgPresent_thenSendToExchange_andAck() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);
            util.when(() -> RabbitIntegrationUtil.stripPayload(anyString())).thenAnswer(inv -> inv.getArgument(0));

            String strategyName = "router-strat";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            doThrow(new RabbitExceptionRouterIntegration("test", "target-strategy")).when(strategy).messageConsumer(anyString(), any(), anyMap());
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
            cfg.setName(strategyName);
            cfg.setExchange("exr");
            cfg.setRoutingKey("rkr");
            consumersProperties.setConsumers(List.of(cfg));

            Message msg = makeMessage("body", Map.of("strategy", strategyName), 21L);

            // expect send to normal exchange (not .retry/.dlq)
            doNothing().when(rabbitTemplate).convertAndSend(eq("exr"), eq("rkr"), eq("body"), any(MessagePostProcessor.class));

            consumerIntegration.onMessage(msg, channel);

            verify(rabbitTemplate).convertAndSend(eq("exr"), eq("rkr"), eq("body"), any(MessagePostProcessor.class));
            verify(channel).basicAck(21L, false);
        }
    }

    @Test
    void whenStrategyThrowsRouterIntegration_andCfgMissing_thenDoLogOnly() throws Exception {
        try (MockedStatic<RabbitIntegrationUtil> util = mockStatic(RabbitIntegrationUtil.class)) {
            util.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);

            String strategyName = "router-no-cfg";
            RabbitConsumerStrategy strategy = mock(RabbitConsumerStrategy.class);
            doThrow(new RabbitExceptionRouterIntegration("test", "tgt")).when(strategy).messageConsumer(anyString(), any(), anyMap());
            when(registry.getByStrategyName(strategyName)).thenReturn(strategy);

            consumersProperties.setConsumers(Collections.emptyList());

            Message msg = makeMessage("body", Map.of("strategy", strategyName), 31L);

            consumerIntegration.onMessage(msg, channel);

            // No send and no ack (since routerTo logs critical missing cfg)
            verifyNoInteractions(rabbitTemplate);
            verify(channel, never()).basicAck(anyLong(), anyBoolean());
        }
    }
}
