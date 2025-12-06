package com.huntercodexs.integration.servicebus.consumer.implement;

import com.huntercodexs.integration.servicebus.context.ServiceBusErrorContextIntegration;
import com.huntercodexs.integration.servicebus.context.ServiceBusMessageContextIntegration;

public interface ServiceBusProcessorIntegration {

    boolean supports(String queueName);
    void processMessage(ServiceBusMessageContextIntegration context);
    void processError(ServiceBusErrorContextIntegration context);

}
