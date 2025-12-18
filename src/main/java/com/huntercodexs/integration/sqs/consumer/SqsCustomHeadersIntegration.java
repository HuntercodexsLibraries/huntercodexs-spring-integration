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
        headers.setUrl(String.valueOf(message.getHeaders().get("Sqs_QueueUrl")));
        headers.setReceivedAt(String.valueOf(message.getHeaders().get("Sqs_ReceivedAt")));
        headers.setReceivedCount(Integer.parseInt(String.valueOf(message.getHeaders().get("Sqs_Msa_ApproximateReceiveCount"))));
        headers.setSentTimestamp(String.valueOf(message.getHeaders().get("Sqs_Msa_SentTimestamp")));
        headers.setApproximateFirstReceiveTimestamp(String.valueOf(message.getHeaders().get("Sqs_Msa_ApproximateFirstReceiveTimestamp")));
        headers.setQueueName(String.valueOf(message.getHeaders().get("Sqs_QueueName")));
        headers.setAccountId(String.valueOf(message.getHeaders().get("Sqs_Msa_SenderId")));
        headers.setMessageId(String.valueOf(message.getHeaders().get("id")));
        headers.setContentType(String.valueOf(message.getHeaders().get("contentType")));
        return headers;
    }
}
