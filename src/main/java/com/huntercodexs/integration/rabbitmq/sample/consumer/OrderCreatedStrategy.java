//package com.huntercodexs.integration.rabbitmq.producer.sample.consumer;
//
//import com.huntercodexs.integration.rabbitmq.consumer.implement.RabbitConsumerStrategy;
//import org.springframework.amqp.core.Message;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Component
//public class OrderCreatedStrategy implements RabbitConsumerStrategy {
//
//    @Override
//    public String getName() {
//        return "ORDER_CREATED";
//    }
//
//    @Override
//    public void handle(String payload, Message originalMessage, Map<String, Object> headers) throws Exception {
//        // parse payload if JSON, for demo do simple print
//        System.out.println("[OrderCreated] payload=" + payload);
//        // simulate processing...
//        if (payload.contains("erro")) {
//            throw new RuntimeException("Simulated processing error");
//        }
//    }
//}
