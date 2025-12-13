package com.huntercodexs.integration.rabbitmq.consumer.implement;

import org.springframework.amqp.core.Message;

import java.util.Map;

public interface RabbitConsumerStrategy extends MessageConsumerStrategy<String> {
    // uses String payload by default (raw body). Implementations can parse JSON to POJO if needed.
    @Override
    String getName();

    @Override
    void handle(String payload, Message originalMessage, Map<String,Object> headers) throws Exception;
}
