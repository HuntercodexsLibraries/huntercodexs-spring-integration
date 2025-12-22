package com.huntercodexs.integration.sqs.consumer;

import com.huntercodexs.integration.sqs.consumer.implement.SqsConsumerIntegration;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huntercodexs.integration.sqs.consumer.SqsCustomHeadersIntegration.fromMessageHeaders;
import static com.huntercodexs.integration.sqs.consumer.SqsCustomHeadersIntegration.getValueOrDefault;

@Component
@SuppressWarnings("java:S2139")
public class SqsDynamicConsumerIntegration {

    private static final Logger log = LoggerFactory.getLogger(SqsDynamicConsumerIntegration.class);

    private final List<SqsConsumerIntegration> consumers;

    public SqsDynamicConsumerIntegration(List<SqsConsumerIntegration> consumers) {
        this.consumers = consumers;
    }

    @SqsListener(queueNames = "#{@dynamicSqsQueuesConsumer}")
    public void consumer(String payload, Message<?> message) {

        String queueName = "";
        SqsCustomHeadersIntegration headers = fromMessageHeaders(message);

        try {
            queueName = getValueOrDefault(message, "x-queue-name");
        } catch (Exception e) {
            log.error("Error when retrieving queue name from headers: {}", e.getMessage());
            throw new IllegalStateException("Error when retrieving queue name from headers", e);
        }

        if (queueName == null || queueName.isEmpty()) {
            log.error("Queue name is empty in headers");
            throw new IllegalStateException("Queue name is empty in headers");
        }

        log.info("Received message from queue:{}, attempts:{}, payload:{}", queueName, headers.getReceivedCount(), payload);

        String finalQueueName = queueName;

        SqsConsumerIntegration current = consumers.stream()
                .filter(c -> c.supports(finalQueueName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Consumer not found for the queue: " + finalQueueName));

        current.consumer(payload, headers);

        log.info("Message from queue {} processed successfully", queueName);
    }

}
