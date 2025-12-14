package com.huntercodexs.integration.rabbitmq.consumer;

import org.springframework.amqp.core.Message;

import java.util.Map;

public interface RabbitConsumerStrategy extends MessageConsumerStrategy<String> {

    @Override
    String supports();

    @Override
    void messageConsumer(String payload, Message originalMessage, Map<String,Object> headers) throws Exception;

}
