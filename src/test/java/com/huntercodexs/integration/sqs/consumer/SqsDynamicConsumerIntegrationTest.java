package com.huntercodexs.integration.sqs.consumer;

import com.huntercodexs.integration.sqs.consumer.implement.SqsConsumerIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.messaging.Message;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SqsDynamicConsumerIntegrationTest {

    private SqsConsumerIntegration mockConsumer;
    private List<SqsConsumerIntegration> consumers;
    private SqsDynamicConsumerIntegration integration;

    @BeforeEach
    void setup() {
        mockConsumer = mock(SqsConsumerIntegration.class);
        consumers = new ArrayList<>();
        integration = new SqsDynamicConsumerIntegration(consumers);
    }

    @Test
    void consumer_successfulProcessing() {
        consumers.add(mockConsumer);
        when(mockConsumer.supports("queue1")).thenReturn(true);

        SqsCustomHeadersIntegration headers = mock(SqsCustomHeadersIntegration.class);
        when(headers.getReceivedCount()).thenReturn(2);

        Message<?> message = mock(Message.class);

        try (MockedStatic<SqsCustomHeadersIntegration> util = mockStatic(SqsCustomHeadersIntegration.class)) {
            util.when(() -> SqsCustomHeadersIntegration.fromMessageHeaders(message)).thenReturn(headers);
            util.when(() -> SqsCustomHeadersIntegration.getValueOrDefault(message, "x-queue-name")).thenReturn("queue1");

            integration.consumer("payload", message);

            verify(mockConsumer).consumer("payload", headers);
        }
    }

    @Test
    void consumer_queueNameMissing_throws() {
        Message<?> message = mock(Message.class);

        try (MockedStatic<SqsCustomHeadersIntegration> util = mockStatic(SqsCustomHeadersIntegration.class)) {
            util.when(() -> SqsCustomHeadersIntegration.fromMessageHeaders(message)).thenReturn(mock(SqsCustomHeadersIntegration.class));
            util.when(() -> SqsCustomHeadersIntegration.getValueOrDefault(message, "x-queue-name")).thenReturn("");

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    integration.consumer("payload", message)
            );
            assertTrue(ex.getMessage().contains("Queue name is empty in headers"));
        }
    }

    @Test
    void consumer_queueNameNull_throws() {
        Message<?> message = mock(Message.class);

        try (MockedStatic<SqsCustomHeadersIntegration> util = mockStatic(SqsCustomHeadersIntegration.class)) {
            util.when(() -> SqsCustomHeadersIntegration.fromMessageHeaders(message)).thenReturn(mock(SqsCustomHeadersIntegration.class));
            util.when(() -> SqsCustomHeadersIntegration.getValueOrDefault(message, "x-queue-name")).thenReturn(null);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    integration.consumer("payload", message)
            );
            assertTrue(ex.getMessage().contains("Queue name is empty in headers"));
        }
    }

    @Test
    void consumer_queueNameExtractionThrows_throws() {
        Message<?> message = mock(Message.class);

        try (MockedStatic<SqsCustomHeadersIntegration> util = mockStatic(SqsCustomHeadersIntegration.class)) {
            util.when(() -> SqsCustomHeadersIntegration.fromMessageHeaders(message)).thenReturn(mock(SqsCustomHeadersIntegration.class));
            util.when(() -> SqsCustomHeadersIntegration.getValueOrDefault(message, "x-queue-name"))
                    .thenThrow(new RuntimeException("header error"));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    integration.consumer("payload", message)
            );
            assertTrue(ex.getMessage().contains("Error when retrieving queue name from headers"));
        }
    }

    @Test
    void consumer_noConsumerFound_throws() {
        consumers.add(mockConsumer);
        when(mockConsumer.supports("queueX")).thenReturn(false);

        Message<?> message = mock(Message.class);

        try (MockedStatic<SqsCustomHeadersIntegration> util = mockStatic(SqsCustomHeadersIntegration.class)) {
            util.when(() -> SqsCustomHeadersIntegration.fromMessageHeaders(message)).thenReturn(mock(SqsCustomHeadersIntegration.class));
            util.when(() -> SqsCustomHeadersIntegration.getValueOrDefault(message, "x-queue-name")).thenReturn("queueX");

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    integration.consumer("payload", message)
            );
            assertTrue(ex.getMessage().contains("Consumer not found for the queue: queueX"));
        }
    }
}
