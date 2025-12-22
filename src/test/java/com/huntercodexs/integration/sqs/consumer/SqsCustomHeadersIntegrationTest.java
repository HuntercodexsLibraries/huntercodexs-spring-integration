package com.huntercodexs.integration.sqs.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.*;

class SqsCustomHeadersIntegrationTest {

    @Test
    void fromMessageHeaders_allHeadersPresent() {
        Message<String> message = MessageBuilder.withPayload("payload")
                .setHeader("Sqs_QueueUrl", "https://sqs.aws.com/queue")
                .setHeader("Sqs_ReceivedAt", "2024-06-01T12:00:00Z")
                .setHeader("Sqs_Msa_ApproximateReceiveCount", "5")
                .setHeader("Sqs_Msa_SentTimestamp", "1717238400000")
                .setHeader("Sqs_Msa_ApproximateFirstReceiveTimestamp", "1717238300000")
                .setHeader("Sqs_QueueName", "my-queue")
                .setHeader("Sqs_Msa_SenderId", "123456789012")
                .setHeader("contentType", "application/json")
                .build();

        SqsCustomHeadersIntegration headers = SqsCustomHeadersIntegration.fromMessageHeaders(message);

        assertEquals("https://sqs.aws.com/queue", headers.getUrl());
        assertEquals("2024-06-01T12:00:00Z", headers.getReceivedAt());
        assertEquals(5, headers.getReceivedCount());
        assertEquals("1717238400000", headers.getSentTimestamp());
        assertEquals("1717238300000", headers.getApproximateFirstReceiveTimestamp());
        assertEquals("my-queue", headers.getQueueName());
        assertEquals("123456789012", headers.getAccountId());
        assertEquals("application/json", headers.getContentType());
    }

    @Test
    void fromMessageHeaders_missingHeaders_resultsInNullOrDefault() {
        Message<String> message = MessageBuilder.withPayload("payload")
                .setHeader("Sqs_Msa_ApproximateReceiveCount", "3")
                .build();

        SqsCustomHeadersIntegration headers = SqsCustomHeadersIntegration.fromMessageHeaders(message);

        assertNull(headers.getUrl());
        assertNull(headers.getReceivedAt());
        assertEquals(3, headers.getReceivedCount());
        assertNull(headers.getSentTimestamp());
        assertNull(headers.getApproximateFirstReceiveTimestamp());
        assertNull(headers.getQueueName());
        assertNull(headers.getAccountId());
        assertNull(headers.getContentType());
    }

    @Test
    @SuppressWarnings("java:S5778")
    void fromMessageHeaders_nonIntegerReceiveCount_throwsNumberFormatException() {
        Message<String> message = MessageBuilder.withPayload("payload")
                .setHeader("Sqs_Msa_ApproximateReceiveCount", "notAnInt")
                .build();

        assertThrows(NumberFormatException.class, () -> {
            SqsCustomHeadersIntegration headers = SqsCustomHeadersIntegration.fromMessageHeaders(message);
            headers.getReceivedCount();
        });
    }
}
