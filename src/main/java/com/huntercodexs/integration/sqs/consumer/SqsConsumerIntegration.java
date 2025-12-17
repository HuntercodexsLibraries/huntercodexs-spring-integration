//package com.huntercodexs.integration.sqs.consumer;
//
//import io.awspring.cloud.sqs.annotation.SqsListener;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class SqsConsumerIntegration {
//
//    @SqsListener("${cloud.aws.queue.name}")
//    public void consumer(String payload) {
//        System.out.println("Message received: " + payload);
//    }
//
//}
