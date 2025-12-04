package com.huntercodexs.integration.servicebus.consumer.implement;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
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
    public void processMessage(ServiceBusReceivedMessageContext mensagem) {
        log.warn("Default message processing started for message ID: {}", mensagem.getMessage().getMessageId());
        log.warn("This message was routed to the default processor. No specific processor found for the queue.");
        log.warn("Message will be abandoned.");
        mensagem.abandon();
    }

    @Override
    public void processError(ServiceBusErrorContext serviceBusErrorContext) {
        log.error("Error occurred in Service Bus processing: {}", serviceBusErrorContext.getException().getMessage());
    }

}
