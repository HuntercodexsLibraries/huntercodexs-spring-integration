package com.huntercodexs.integration.sqs.consumer;

import lombok.Data;
import org.springframework.messaging.Message;

@Data
public class SqsCustomHeadersIntegration {
    private String url;
    private String receivedAt;
    private int receivedCount;
    private String sentTimestamp;
    private String approximateFirstReceiveTimestamp;
    private String queueName;
    private String accountId;
    private String messageId;
    private String contentType;

    public static SqsCustomHeadersIntegration fromMessageHeaders(Message<?> message) {
        SqsCustomHeadersIntegration headers = new SqsCustomHeadersIntegration();
        headers.setUrl(getValueOrDefault(message, "Sqs_QueueUrl"));
        headers.setReceivedAt(getValueOrDefault(message, "Sqs_ReceivedAt"));
        headers.setReceivedCount(getValueOrZero(message, "Sqs_Msa_ApproximateReceiveCount"));
        headers.setSentTimestamp(getValueOrDefault(message, "Sqs_Msa_SentTimestamp"));
        headers.setApproximateFirstReceiveTimestamp(getValueOrDefault(message, "Sqs_Msa_ApproximateFirstReceiveTimestamp"));
        headers.setQueueName(getValueOrDefault(message, "Sqs_QueueName"));
        headers.setAccountId(getValueOrDefault(message, "Sqs_Msa_SenderId"));
        headers.setMessageId(getValueOrDefault(message, "id"));
        headers.setContentType(getValueOrDefault(message, "contentType"));
        return headers;
    }

    public static String getValueOrDefault(Message<?> message, String value) {
        return message.getHeaders().get(value) != null ? String.valueOf(message.getHeaders().get(value)) : null;
    }

    private static int getValueOrZero(Message<?> message, String value) {
        return message.getHeaders().get(value) != null ? Integer.parseInt(String.valueOf(message.getHeaders().get(value))) : 0;
    }
}
