package com.huntercodexs.integration.servicebus.context;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusMessageState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceBusMessageContextIntegrationTest {

    private ServiceBusReceivedMessageContext context;
    private ServiceBusReceivedMessage message;
    private AmqpAnnotatedMessage amqpAnnotatedMessage;
    private AmqpMessageBody amqpMessageBody;

    @BeforeEach
    void setUp() {
        context = mock(ServiceBusReceivedMessageContext.class);
        message = mock(ServiceBusReceivedMessage.class);
        amqpAnnotatedMessage = mock(AmqpAnnotatedMessage.class);
        amqpMessageBody = mock(AmqpMessageBody.class);

        when(context.getMessage()).thenReturn(message);

        // Data
        BinaryData body = BinaryData.fromString("{\"key\":\"value\",\"num\":42}");
        when(message.getBody()).thenReturn(body);

        // Details simple fields
        when(message.getSequenceNumber()).thenReturn(123L);
        when(message.getSessionId()).thenReturn("session-1");
        when(message.getTo()).thenReturn("queue-xyz");
        when(message.getTimeToLive()).thenReturn(Duration.ofMinutes(5));
        // State value
        when(message.getState()).thenReturn(ServiceBusMessageState.ACTIVE);
        when(message.getPartitionKey()).thenReturn("pk-1");
        when(message.getLockToken()).thenReturn("lock-abc");
        OffsetDateTime now = OffsetDateTime.now();
        when(message.getLockedUntil()).thenReturn(now.plusMinutes(1));
        when(message.getExpiresAt()).thenReturn(now.plusMinutes(10));
        when(message.getEnqueuedTime()).thenReturn(now.minusMinutes(2));
        when(message.getEnqueuedSequenceNumber()).thenReturn(999L);
        when(message.getDeadLetterReason()).thenReturn("reason-x");
        when(message.getDeadLetterErrorDescription()).thenReturn("desc-y");
        when(message.getCorrelationId()).thenReturn("corr-123");
        Map<String, Object> appProps = new HashMap<>();
        appProps.put("p1", "v1");
        appProps.put("p2", 2);
        when(message.getApplicationProperties()).thenReturn(appProps);
        when(message.getContentType()).thenReturn("application/json");
        when(message.getDeliveryCount()).thenReturn(3L);
        when(message.getMessageId()).thenReturn("msg-789");

        // Raw AMQP message, annotations and body
        Map<String, Object> annotations = new HashMap<>();
        annotations.put("anno1", "A");
        annotations.put("anno2", 10);
        when(message.getRawAmqpMessage()).thenReturn(amqpAnnotatedMessage);
        when(amqpAnnotatedMessage.getMessageAnnotations()).thenReturn(annotations);
        when(amqpAnnotatedMessage.getBody()).thenReturn(amqpMessageBody);

        // Body value and data
        when(amqpMessageBody.getValue()).thenReturn("raw-value");
        IterableStream<byte[]> dataStream = new IterableStream<>(Arrays.asList("d1".getBytes(), "d2".getBytes()));
        when(amqpMessageBody.getData()).thenReturn(dataStream);
    }

    @Test
    void data_should_return_body_as_bytes_string_and_object() {
        ServiceBusMessageContextIntegration wrapper = new ServiceBusMessageContextIntegration(context);

        byte[] bytes = wrapper.getData().getBodyAsBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        String str = wrapper.getData().getBodyAsString();
        assertTrue(str.contains("key"));

        Map obj = wrapper.getData().getBodyAsObject(Map.class);
        assertEquals("value", obj.get("key"));
        assertEquals(42, obj.get("num"));
    }

    @Test
    void details_should_load_all_fields() {
        ServiceBusMessageContextIntegration wrapper = new ServiceBusMessageContextIntegration(context);
        ServiceBusMessageContextIntegration.Details d = wrapper.getDetails();

        assertEquals(123L, d.getSequenceNumber());
        assertEquals("session-1", d.getSessionId());
        assertEquals("queue-xyz", d.getTo());
        assertEquals(Duration.ofMinutes(5), d.getTimeToLive());
        assertEquals(ServiceBusMessageState.ACTIVE.getValue(), d.getValue());
        assertEquals("pk-1", d.getPartitionKey());
        assertEquals("lock-abc", d.getLockToken());
        assertNotNull(d.getLockedUntil());
        assertNotNull(d.getExpiresAt());
        assertNotNull(d.getEnqueuedTime());
        assertEquals(999L, d.getEnqueuedSequenceNumber());
        assertEquals("reason-x", d.getDeadLetterReason());
        assertEquals("desc-y", d.getDeadLetterErrorDescription());
        assertEquals("corr-123", d.getCorrelationId());
        assertEquals("application/json", d.getContentType());
        assertEquals(3L, d.getDeliveryCount());
        assertEquals("msg-789", d.getMessageId());

        assertNotNull(d.getApplicationProperties());
        assertEquals(2, d.getApplicationProperties().size());
        assertNotNull(d.getMessageAnnotations());
        assertEquals(2, d.getMessageAnnotations().size());
        assertEquals("raw-value", d.getRawAmqpMessageValue());

        List<byte[]> collected = new ArrayList<>();
        d.getRawAmqpMessageData().forEach(collected::add);
        assertEquals(2, collected.size());
    }

    @Test
    void actions_should_invoke_context_methods() {
        ServiceBusMessageContextIntegration wrapper = new ServiceBusMessageContextIntegration(context);

        wrapper.getActions().complete();
        wrapper.getActions().abandon();
        wrapper.getActions().deadLetter();
        wrapper.getActions().defer();

        verify(context, times(1)).complete();
        verify(context, times(1)).abandon();
        verify(context, times(1)).deadLetter();
        verify(context, times(1)).defer();
    }
}