package com.huntercodexs.integration.rabbitmq.producer;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitProducersIntegrationProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil.stripPayload;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitProducerIntegrationTest {

    @Mock RabbitTemplate rabbitTemplate;
    @Mock RabbitGlobalIntegrationProperties globalProperties;
    @Mock RabbitProducersIntegrationProperties producersProperties;

    static MockedStatic<com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil> utilMock;

    @BeforeAll
    static void beforeAll() {
        utilMock = mockStatic(com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil.class);
    }

    @AfterAll
    static void afterAll() {
        utilMock.close();
    }

    @BeforeEach
    void setup() {
        utilMock.when(() -> stripPayload(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void send_usesDefaultProducerConfig_persistent() {
        when(producersProperties.getName()).thenReturn("default-strategy");
        when(producersProperties.getExchange()).thenReturn("ex");
        when(producersProperties.getRoutingKey()).thenReturn("rk");
        when(producersProperties.getDeliveryMode()).thenReturn("PERSISTENT");
        when(producersProperties.getProducers()).thenReturn(Collections.emptyList());

        RabbitProducerIntegration producer = new RabbitProducerIntegration(rabbitTemplate, globalProperties, producersProperties);

        producer.send("default-strategy", "payload");

        verify(rabbitTemplate).convertAndSend(
                eq("ex"),
                eq("rk"),
                eq("payload"),
                any(MessagePostProcessor.class));
    }

    @Test
    void send_usesDefaultProducerConfig_nonPersistent() {
        when(producersProperties.getName()).thenReturn("default-strategy");
        when(producersProperties.getExchange()).thenReturn("ex");
        when(producersProperties.getRoutingKey()).thenReturn("rk");
        when(producersProperties.getDeliveryMode()).thenReturn("NON_PERSISTENT");
        when(producersProperties.getProducers()).thenReturn(Collections.emptyList());

        RabbitProducerIntegration producer = new RabbitProducerIntegration(rabbitTemplate, globalProperties, producersProperties);

        producer.send("default-strategy", "payload");

        ArgumentCaptor<java.util.function.Function<Message, Message>> captor = ArgumentCaptor.forClass(java.util.function.Function.class);

        verify(rabbitTemplate).convertAndSend(
                eq("ex"),
                eq("rk"),
                eq("payload"),
                any(MessagePostProcessor.class));
    }

    @Test
    void send_usesNestedProducerConfig_persistent() {
        RabbitProducersIntegrationProperties nested = mock(RabbitProducersIntegrationProperties.class);
        when(nested.getName()).thenReturn("nested-strategy");
        when(nested.getExchange()).thenReturn("ex2");
        when(nested.getRoutingKey()).thenReturn("rk2");
        when(nested.getDeliveryMode()).thenReturn("PERSISTENT");

        when(producersProperties.getName()).thenReturn("default-strategy");
        when(producersProperties.getProducers()).thenReturn(List.of(nested));

        RabbitProducerIntegration producer = new RabbitProducerIntegration(rabbitTemplate, globalProperties, producersProperties);

        producer.send("nested-strategy", "payload");

        verify(rabbitTemplate).convertAndSend(
                eq("ex2"),
                eq("rk2"),
                eq("payload"),
                any(MessagePostProcessor.class));
    }

    @Test
    void send_usesNestedProducerConfig_nonPersistent() {
        RabbitProducersIntegrationProperties nested = mock(RabbitProducersIntegrationProperties.class);
        when(nested.getName()).thenReturn("nested-strategy");
        when(nested.getExchange()).thenReturn("ex2");
        when(nested.getRoutingKey()).thenReturn("rk2");
        when(nested.getDeliveryMode()).thenReturn("NON_PERSISTENT");

        when(producersProperties.getName()).thenReturn("default-strategy");
        when(producersProperties.getProducers()).thenReturn(List.of(nested));

        RabbitProducerIntegration producer = new RabbitProducerIntegration(rabbitTemplate, globalProperties, producersProperties);

        producer.send("nested-strategy", "payload");

        verify(rabbitTemplate).convertAndSend(
                eq("ex2"),
                eq("rk2"),
                eq("payload"),
                any(MessagePostProcessor.class));
    }

    @Test
    void send_withExtraHeaders_setsAllHeaders() {
        RabbitProducersIntegrationProperties nested = mock(RabbitProducersIntegrationProperties.class);
        when(nested.getName()).thenReturn("nested-strategy");
        when(nested.getExchange()).thenReturn("ex2");
        when(nested.getRoutingKey()).thenReturn("rk2");
        when(nested.getDeliveryMode()).thenReturn("PERSISTENT");

        when(producersProperties.getName()).thenReturn("default-strategy");
        when(producersProperties.getProducers()).thenReturn(List.of(nested));

        RabbitProducerIntegration producer = new RabbitProducerIntegration(rabbitTemplate, globalProperties, producersProperties);

        Map<String, Object> extraHeaders = new HashMap<>();
        extraHeaders.put("foo", "bar");
        extraHeaders.put("baz", 123);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        producer.send("nested-strategy", "payload", extraHeaders);

        verify(rabbitTemplate).convertAndSend(
                eq("ex2"),
                eq("rk2"),
                eq("payload"),
                captor.capture());

        // Simulate the message post-processor
        MessageProperties props = new MessageProperties();
        Message msg = new Message("payload".getBytes(), props);
        Message processed = captor.getValue().postProcessMessage(msg);

        assertEquals("nested-strategy", processed.getMessageProperties().getHeaders().get("strategy"));
        assertNotNull(processed.getMessageProperties().getHeaders().get("messageId"));
        assertEquals("bar", processed.getMessageProperties().getHeaders().get("foo"));
        assertEquals(123, processed.getMessageProperties().getHeaders().get("baz"));
        assertEquals(MessageDeliveryMode.PERSISTENT, processed.getMessageProperties().getDeliveryMode());
    }

    @Test
    void send_throwsException_whenConfigNotFound() {
        when(producersProperties.getName()).thenReturn("default-strategy");
        when(producersProperties.getProducers()).thenReturn(Collections.emptyList());

        RabbitProducerIntegration producer = new RabbitProducerIntegration(rabbitTemplate, globalProperties, producersProperties);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                producer.send("unknown-strategy", "payload")
        );
        assertTrue(ex.getMessage().contains("Configuration not found for producer strategy"));
    }
}
