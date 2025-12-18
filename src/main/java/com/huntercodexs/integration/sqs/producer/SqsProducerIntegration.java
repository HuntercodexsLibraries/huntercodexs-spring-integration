package com.huntercodexs.integration.sqs.producer;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class SqsProducerIntegration {

    private static final Logger log = LoggerFactory.getLogger(SqsProducerIntegration.class);

    @Value("${cloud.aws.endpoint.uri:}")
    private String endpointUri;

    @Value("${cloud.aws.account-id:}")
    private String accountId;

    @Value("${cloud.aws.region.static:}")
    private String region;

    @Autowired
    SqsTemplate sqsTemplate;

    public void send(String payload, String queue) {

        String destination = "https://sqs."+region+".amazonaws.com/"+accountId+"/"+queue;

        if (endpointUri != null && !endpointUri.isEmpty()) {
            destination = endpointUri+accountId+"/"+queue;
        }

        try {

            Message<?> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader("x-queue-name", queue)
                    .build();

            sqsTemplate.send(destination, message);
            log.info("Message sent to SQS queue {} successfully", queue);
        } catch (RuntimeException re) {
            log.error("SQS Message failed during send: {}", re.getMessage());
            throw new RuntimeException();
        }

    }

}
