package com.huntercodexs.integration.servicebus.producer.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceBusProducerConfigTest {

    @Mock
    ServiceBusClientBuilder builder;

    @Mock
    ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderBuilder;

    @Mock
    ServiceBusSenderClient senderClient;

    @Test
    void deveConstruirClientComTopicNameQuandoInformado() {
        ServiceBusProducerConfig config = new ServiceBusProducerConfig();
        ReflectionTestUtils.setField(config, "connectionString", "Endpoint=sb://fake.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fake");
        ReflectionTestUtils.setField(config, "serviceBusTopicName", "meu-topico");
        ReflectionTestUtils.setField(config, "serviceBusQueueName", "minha-fila");

        when(builder.connectionString(anyString())).thenReturn(builder);
        when(builder.sender()).thenReturn(senderBuilder);
        when(senderBuilder.topicName(anyString())).thenReturn(senderBuilder);
        when(senderBuilder.buildClient()).thenReturn(senderClient);

        ServiceBusSenderClient result = config.serviceBusIntegrationProducerClient(builder);

        assertNotNull(result);

        InOrder inOrder = inOrder(builder, senderBuilder);
        inOrder.verify(builder).connectionString("Endpoint=sb://fake.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fake");
        inOrder.verify(builder).sender();
        verify(senderBuilder).topicName("meu-topico");
        verify(senderBuilder, never()).queueName(anyString());
        verify(senderBuilder).buildClient();
    }

    @Test
    void deveConstruirClientComQueueNameQuandoTopicNameVazio() {
        ServiceBusProducerConfig config = new ServiceBusProducerConfig();
        ReflectionTestUtils.setField(config, "connectionString", "Endpoint=sb://fake.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fake");
        ReflectionTestUtils.setField(config, "serviceBusTopicName", "");
        ReflectionTestUtils.setField(config, "serviceBusQueueName", "minha-fila");

        when(builder.connectionString(anyString())).thenReturn(builder);
        when(builder.sender()).thenReturn(senderBuilder);
        when(senderBuilder.queueName(anyString())).thenReturn(senderBuilder);
        when(senderBuilder.buildClient()).thenReturn(senderClient);

        ServiceBusSenderClient result = config.serviceBusIntegrationProducerClient(builder);

        assertNotNull(result);

        InOrder inOrder = inOrder(builder, senderBuilder);
        inOrder.verify(builder).connectionString("Endpoint=sb://fake.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fake");
        inOrder.verify(builder).sender();
        verify(senderBuilder).queueName("minha-fila");
        verify(senderBuilder, never()).topicName(anyString());
        verify(senderBuilder).buildClient();
    }
}