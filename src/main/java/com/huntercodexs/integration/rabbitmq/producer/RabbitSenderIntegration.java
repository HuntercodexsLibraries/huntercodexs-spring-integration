package com.huntercodexs.integration.rabbitmq.producer;

import java.util.Map;

public interface RabbitSenderIntegration {
    void send(String strategyName, Object payload);
    void send(String strategyName, Object payload, Map<String,Object> extraHeaders);
}
