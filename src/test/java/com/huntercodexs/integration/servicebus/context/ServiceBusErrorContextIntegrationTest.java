package com.huntercodexs.integration.servicebus.context;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusErrorContextIntegrationTest {

    @Test
    void shouldPopulateFieldsFromErrorContext() {
        ServiceBusErrorContext errorContext = mock(ServiceBusErrorContext.class);
        Throwable exception = new RuntimeException("fail");
        String entityPath = "queue-name";
        String namespace = "sb://namespace.servicebus.windows.net/";
        ServiceBusErrorSource serviceBusErrorSource = new ServiceBusErrorSource();

        when(errorContext.getException()).thenReturn(exception);
        when(errorContext.getEntityPath()).thenReturn(entityPath);
        when(errorContext.getFullyQualifiedNamespace()).thenReturn(namespace);
        when(errorContext.getErrorSource()).thenReturn(serviceBusErrorSource);

        ServiceBusErrorContextIntegration integration = new ServiceBusErrorContextIntegration(errorContext);

        assertSame(errorContext, integration.getErrorContext());
        assertSame(exception, integration.getException());
        assertEquals(entityPath, integration.getEntityPath());
        assertEquals(namespace, integration.getFullyQualifiedNamespace());
    }
}