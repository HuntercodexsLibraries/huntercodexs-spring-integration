package com.huntercodexs.integration.servicebus.consumer.implement;

import com.huntercodexs.integration.servicebus.context.ServiceBusErrorContextIntegration;
import com.huntercodexs.integration.servicebus.context.ServiceBusMessageContextIntegration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.huntercodexs.integration.servicebus.constants.ServiceBusIntegrationConstants.SERVICEBUS_DEFAULT_CONSUMER_QUEUE_NAME;

@Component
@RequiredArgsConstructor
public class ServiceBusProcessorIntegrationDefaultImpl implements ServiceBusProcessorIntegration {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusProcessorIntegrationDefaultImpl.class);

    @Override
    public boolean supports(String queueName) {
        return queueName.equals(SERVICEBUS_DEFAULT_CONSUMER_QUEUE_NAME);
    }

    @Override
    public void processMessage(ServiceBusMessageContextIntegration message) {
        log.warn("Default message processing started for message ID: {}", message.getDetails().getMessageId());
        log.warn("This message was routed to the default processor. No specific processor found for the queue.");
        log.warn("Message will be abandoned.");
        message.getActions().abandon();
    }

    @Override
    public void processError(ServiceBusErrorContextIntegration context) {
        log.error("Error occurred in Service Bus processing: {}", context.getException().getMessage());
    }

}
