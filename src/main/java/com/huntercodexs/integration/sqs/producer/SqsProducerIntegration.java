//package com.huntercodexs.integration.sqs.producer;
//
//import io.awspring.cloud.sqs.operations.SqsTemplate;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class SqsProducerIntegration {
//
//    @Autowired
//    SqsTemplate sqsTemplate;
//
//    public void publisher(String message) {
//        sqsTemplate.send(message);
//        System.out.println("Message Publisher");
//        System.out.println("Message: " + message);
//    }
//
//}
