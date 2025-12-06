package com.huntercodexs.integration.servicebus.context;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
public class ServiceBusMessageContextIntegration {

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
            this.messageAnnotations = messageContext.getMessage().getRawAmqpMessage().getMessageAnnotations();
            this.rawAmqpMessageValue = messageContext.getMessage().getRawAmqpMessage().getBody().getValue();
            this.rawAmqpMessageData = messageContext.getMessage().getRawAmqpMessage().getBody().getData();
            this.deliveryCount = messageContext.getMessage().getDeliveryCount();
            this.messageId = messageContext.getMessage().getMessageId();
        }
    }

    public static class Actions {

        ServiceBusReceivedMessageContext messageContext;

        public Actions(ServiceBusReceivedMessageContext messageContext) {
            this.messageContext = messageContext;
        }

        public void complete() {
            messageContext.complete();
        }

        public void abandon() {
            messageContext.abandon();
        }

        public void deadLetter() {
            messageContext.deadLetter();
        }

        public void defer() {
            messageContext.defer();
        }
    }
}
