package com.huntercodexs.integration.rabbitmq.producer;

import java.util.Map;

interface RabbitProducerIntegration {
    void send(String strategyName, Object payload);
    void send(String strategyName, Object payload, Map<String,Object> extraHeaders);
}
