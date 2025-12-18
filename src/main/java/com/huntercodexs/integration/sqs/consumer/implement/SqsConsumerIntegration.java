package com.huntercodexs.integration.sqs.consumer.implement;

import com.huntercodexs.integration.sqs.consumer.SqsCustomHeadersIntegration;

public interface SqsConsumerIntegration {

    boolean supports(String queueName);
    void consumer(String payload, SqsCustomHeadersIntegration headers);

}
