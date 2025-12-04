package com.huntercodexs.integration.servicebus.consumer.implement;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

public interface ServiceBusProcessorIntegration {

    boolean supports(String queueName);
    void processMessage(ServiceBusReceivedMessageContext context);
    void processError(ServiceBusErrorContext context);

}
