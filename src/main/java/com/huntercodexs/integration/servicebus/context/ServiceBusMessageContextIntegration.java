package com.huntercodexs.integration.servicebus.context;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
public class ServiceBusMessageContextIntegration {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusMessageContextIntegration.class);

    private final Data data;
    private final Details details;
    private final Actions actions;

    @Getter(AccessLevel.NONE)
    private final ServiceBusReceivedMessageContext messageContext;

    public ServiceBusMessageContextIntegration(ServiceBusReceivedMessageContext messageContext) {
        this.messageContext = messageContext;

        this.data = new Data(messageContext);
        this.details = new Details(messageContext);
        this.actions = new Actions(messageContext);
    }

    public static class Data {

        ServiceBusReceivedMessageContext messageContext;

        public Data(ServiceBusReceivedMessageContext messageContext) {
            this.messageContext = messageContext;
        }

        public byte[] getBodyAsBytes() {
            return messageContext.getMessage().getBody().toBytes();
        }

        public String getBodyAsString() {
            return messageContext.getMessage().getBody().toString();
        }

        public <T> T getBodyAsObject(Class<T> clazz) {
            return messageContext.getMessage().getBody().toObject(clazz);
        }
    }

    @Getter
    public static class Details {

        private long sequenceNumber;
        private String sessionId;
        private String to;
        private Duration timeToLive;
        private int value;
        private String partitionKey;
        private String lockToken;
        private OffsetDateTime lockedUntil;
        private OffsetDateTime expiresAt;
        private OffsetDateTime enqueuedTime;
        private long enqueuedSequenceNumber;
        private String deadLetterReason;
        private String deadLetterErrorDescription;
        private String correlationId;
        private Map<String, Object> applicationProperties;
        private String contentType;
        private Map<String, Object> messageAnnotations;
        private Object rawAmqpMessageValue;
        private IterableStream<byte[]> rawAmqpMessageData;
        private long deliveryCount;
        private String messageId;

        ServiceBusReceivedMessageContext messageContext;

        public Details(ServiceBusReceivedMessageContext messageContext) {
            this.messageContext = messageContext;
            this.detailsLoader();
        }

        private void detailsLoader() {

            log.info("Loading message details for message ID: {}", messageContext.getMessage().getMessageId());

            this.sequenceNumber = messageContext.getMessage().getSequenceNumber();
            this.sessionId = messageContext.getMessage().getSessionId();
            this.to = messageContext.getMessage().getTo();
            this.timeToLive = messageContext.getMessage().getTimeToLive();
            this.value = messageContext.getMessage().getState().getValue();
            this.partitionKey = messageContext.getMessage().getPartitionKey();
            this.lockToken = messageContext.getMessage().getLockToken();
            this.lockedUntil = messageContext.getMessage().getLockedUntil();
            this.expiresAt = messageContext.getMessage().getExpiresAt();
            this.enqueuedTime = messageContext.getMessage().getEnqueuedTime();
            this.enqueuedSequenceNumber = messageContext.getMessage().getEnqueuedSequenceNumber();
            this.deadLetterReason = messageContext.getMessage().getDeadLetterReason();
            this.deadLetterErrorDescription = messageContext.getMessage().getDeadLetterErrorDescription();
            this.correlationId = messageContext.getMessage().getCorrelationId();
            this.applicationProperties = messageContext.getMessage().getApplicationProperties();
            this.contentType = messageContext.getMessage().getContentType();
            this.deliveryCount = messageContext.getMessage().getDeliveryCount();
            this.messageId = messageContext.getMessage().getMessageId();

            try {
                this.messageAnnotations = messageContext.getMessage().getRawAmqpMessage().getMessageAnnotations();
            } catch (Exception e) {
                log.warn("Failed to load Message Annotations for message ID: {}. Error: {}", messageContext.getMessage().getMessageId(), e.getMessage());
            }

            try {
                this.rawAmqpMessageValue = messageContext.getMessage().getRawAmqpMessage().getBody().getValue();
            } catch (Exception e) {
                log.warn("Failed to load Raw AMQP message details for message ID: {}. Error: {}", messageContext.getMessage().getMessageId(), e.getMessage());
            }

            try {
                this.rawAmqpMessageData = messageContext.getMessage().getRawAmqpMessage().getBody().getData();
            } catch (Exception e) {
                log.warn("Failed to load Raw AMQP message data for message ID: {}. Error: {}", messageContext.getMessage().getMessageId(), e.getMessage());
            }

            log.info("Message details loaded for message ID: {}", messageContext.getMessage().getMessageId());
        }
    }

    public static class Actions {

        ServiceBusReceivedMessageContext messageContext;

        public Actions(ServiceBusReceivedMessageContext messageContext) {
            this.messageContext = messageContext;
        }

        public void complete() {
            messageContext.complete();
            log.info("Message with ID '{}' completed successfully.", messageContext.getMessage().getMessageId());
        }

        public void abandon() {
            messageContext.abandon();
            log.info("Message with ID '{}' abandoned.", messageContext.getMessage().getMessageId());
        }

        public void deadLetter() {
            messageContext.deadLetter();
            log.info("Message with ID '{}' dead-lettered.", messageContext.getMessage().getMessageId());
        }

        public void defer() {
            messageContext.defer();
            log.info("Message with ID '{}' deferred.", messageContext.getMessage().getMessageId());
        }
    }
}
