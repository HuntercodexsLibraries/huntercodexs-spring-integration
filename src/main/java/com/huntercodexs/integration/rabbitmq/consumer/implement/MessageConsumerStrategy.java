package com.huntercodexs.integration.rabbitmq.consumer.implement;

import org.springframework.amqp.core.Message;

import java.util.Map;

public interface MessageConsumerStrategy<T> {
    String getName();
    void handle(T payload, Message originalMessage, Map<String,Object> headers) throws Exception;
}
