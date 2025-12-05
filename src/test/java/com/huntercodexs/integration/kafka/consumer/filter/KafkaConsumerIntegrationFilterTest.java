package com.huntercodexs.integration.kafka.consumer.filter;

import com.huntercodexs.integration.kafka.consumer.process.KafkaConsumerIntegrationProcess;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class KafkaConsumerIntegrationFilterTest {

    private KafkaConsumerIntegrationProcess processMock;
    private KafkaConsumerIntegrationFilter<String, String> filter;
    private ConsumerRecord<String, String> recordMock;
    private Headers headersMock;

    @BeforeEach
    void setUp() {
        processMock = mock(KafkaConsumerIntegrationProcess.class);
        filter = new KafkaConsumerIntegrationFilter<>(List.of(processMock));
        recordMock = mock(ConsumerRecord.class);
        headersMock = mock(Headers.class);

        when(recordMock.headers()).thenReturn(headersMock);
        when(recordMock.value()).thenReturn("value");
        when(recordMock.key()).thenReturn("key");
        when(recordMock.partition()).thenReturn(1);
        when(recordMock.offset()).thenReturn(10L);
    }

    @Test
    void testFilter_NoStrategyFound_ReturnsTrue() {
        when(processMock.supports(any(), any(), any(), anyInt(), anyLong())).thenReturn(false);

        boolean result = filter.filter(recordMock);

        assertTrue(result);
        verify(processMock).supports(headersMock, "value", "key", 1, 10L);
    }

    @Test
    void testFilter_StrategyFound_DiscardReturnsTrue() {
        when(processMock.supports(any(), any(), any(), anyInt(), anyLong())).thenReturn(true);
        when(processMock.discard(any(), any(), any(), anyInt(), anyLong())).thenReturn(true);

        boolean result = filter.filter(recordMock);

        assertTrue(result);
        verify(processMock).supports(headersMock, "value", "key", 1, 10L);
        verify(processMock).discard(headersMock, "value", "key", 1, 10L);
    }

    @Test
    void testFilter_StrategyFound_DiscardReturnsFalse() {
        when(processMock.supports(any(), any(), any(), anyInt(), anyLong())).thenReturn(true);
        when(processMock.discard(any(), any(), any(), anyInt(), anyLong())).thenReturn(false);

        boolean result = filter.filter(recordMock);

        assertFalse(result);
        verify(processMock).supports(headersMock, "value", "key", 1, 10L);
        verify(processMock).discard(headersMock, "value", "key", 1, 10L);
    }
}