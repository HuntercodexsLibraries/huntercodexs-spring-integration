package com.huntercodexs.integration.rabbitmq.sample.producer;

import com.huntercodexs.integration.rabbitmq.producer.RabbitSenderIntegrationImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProducerSample {

    private final RabbitSenderIntegrationImpl producer;

    public void demoOrder() {
        producer.send("ORDER_CREATED", "{\"orderId\":\"o-1\",\"product\":\"book\",\"quantity\":1}");
    }

    public void demoUser() {
        producer.send("USER_REGISTERED", "{\"userId\":\"u-1\",\"name\":\"Ana\",\"email\":\"ana@example.com\"}");
    }
}
