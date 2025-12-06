package com.huntercodexs.integration.servicebus.context;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import lombok.Getter;

@Getter
public class ServiceBusErrorContextIntegration {

    private final Throwable exception;
    private final String entityPath;
    private final String errorSourceValue;
    private final String fullyQualifiedNamespace;

    private final ServiceBusErrorContext errorContext;

    public ServiceBusErrorContextIntegration(ServiceBusErrorContext errorContext) {
        this.errorContext = errorContext;
        this.exception = errorContext.getException();
        this.entityPath = errorContext.getEntityPath();
        this.errorSourceValue = errorContext.getErrorSource().getValue();
        this.fullyQualifiedNamespace = errorContext.getFullyQualifiedNamespace();
    }

}
