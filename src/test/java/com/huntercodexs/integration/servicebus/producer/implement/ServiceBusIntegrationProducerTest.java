package com.huntercodexs.integration.servicebus.producer.implement;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceBusIntegrationProducerTest {

    private ServiceBusSenderClient senderClient;
    private ServiceBusIntegrationProducer producer;

    @BeforeEach
    void setUp() {
        senderClient = mock(ServiceBusSenderClient.class);
        producer = new ServiceBusIntegrationProducer(senderClient);
    }

    private void setField(Object target, String fieldName, String value) throws Exception {
        Field f = ServiceBusIntegrationProducer.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void send_ShouldReturnTrue_WithDelaySecondsAndHeadersAndClazz() throws Exception {
        setField(producer, "delaySeconds", "2");
        setField(producer, "delayMinutes", null);

        Map<String, String> headers = new HashMap<>();
        headers.put("key1", "value1");
        headers.put("key2", "value2");

        Map<String, Object> message = new HashMap<>();
        message.put("field", "data");

        ArgumentCaptor<ServiceBusMessage> captor = ArgumentCaptor.forClass(ServiceBusMessage.class);

        boolean result = producer.send(headers, 3L, message, Map.class);

        assertTrue(result);
        verify(senderClient, times(1)).sendMessage(captor.capture());
        ServiceBusMessage sent = captor.getValue();

        assertEquals("value1", sent.getApplicationProperties().get("key1"));
        assertEquals("value2", sent.getApplicationProperties().get("key2"));
        assertEquals(3L, sent.getApplicationProperties().get("attempts"));

        OffsetDateTime scheduled = sent.getScheduledEnqueueTime();
        assertNotNull(scheduled);
        assertTrue(scheduled.isAfter(OffsetDateTime.now().minusSeconds(1)));
    }

    @Test
    void send_ShouldReturnTrue_WithDelayMinutes_NoHeaders_NoClazz() throws Exception {
        setField(producer, "delaySeconds", "");
        setField(producer, "delayMinutes", "1");

        ArgumentCaptor<ServiceBusMessage> captor = ArgumentCaptor.forClass(ServiceBusMessage.class);

        boolean result = producer.send(null, 1L, "plain-message", null);

        assertTrue(result);
        verify(senderClient, times(1)).sendMessage(captor.capture());
        ServiceBusMessage sent = captor.getValue();

        assertEquals(1L, sent.getApplicationProperties().get("attempts"));
        assertFalse(sent.getApplicationProperties().containsKey("key1"));
        assertNotNull(sent.getScheduledEnqueueTime());
    }

    @Test
    void send_ShouldReturnTrue_WithoutDelay_WhenPropertiesEmpty() throws Exception {
        setField(producer, "delaySeconds", "");
        setField(producer, "delayMinutes", "");

        ArgumentCaptor<ServiceBusMessage> captor = ArgumentCaptor.forClass(ServiceBusMessage.class);

        boolean result = producer.send(new HashMap<>(), 0L, "msg", null);

        assertTrue(result);
        verify(senderClient, times(1)).sendMessage(captor.capture());
        ServiceBusMessage sent = captor.getValue();

        assertEquals(0L, sent.getApplicationProperties().get("attempts"));
        assertNull(sent.getScheduledEnqueueTime());
    }

    @Test
    void send_ShouldReturnFalse_WhenClientThrowsException() throws Exception {
        setField(producer, "delaySeconds", "");
        setField(producer, "delayMinutes", "");

        doThrow(new RuntimeException("failure")).when(senderClient).sendMessage(any(ServiceBusMessage.class));

        boolean result = producer.send(null, 5L, "error", null);

        assertFalse(result);
        verify(senderClient, times(1)).sendMessage(any(ServiceBusMessage.class));
    }
}
