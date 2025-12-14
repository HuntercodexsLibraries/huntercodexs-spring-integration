package com.huntercodexs.integration.rabbitmq.consumer;

import org.springframework.amqp.core.Message;

import java.util.Map;

interface MessageConsumerStrategy<T> {

    String supports();
    void messageConsumer(T payload, Message originalMessage, Map<String,Object> headers) throws Exception;

}
