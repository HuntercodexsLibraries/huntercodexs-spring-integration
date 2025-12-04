package com.huntercodexs.integration.servicebus.producer.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.huntercodexs.integration.servicebus.constants.ServiceBusIntegrationConstants.SERVICEBUS_DEFAULT_PRODUCER_QUEUE_NAME;
import static com.huntercodexs.integration.servicebus.constants.ServiceBusIntegrationConstants.SERVICEBUS_SPRING_CLOUD_APP_CONFIG;

@Configuration
public class ServiceBusProducerConfig {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusProducerConfig.class);

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".connection-string}")
    private String connectionString;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".producer.target-queue-name:"+ SERVICEBUS_DEFAULT_PRODUCER_QUEUE_NAME +"}")
    private String serviceBusQueueName;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".producer.topic-name:}")
    private String serviceBusTopicName;

    @Bean
    public ServiceBusSenderClient serviceBusIntegrationProducerClient(ServiceBusClientBuilder builder) {

        ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClient = builder
                .connectionString(connectionString).sender();

        if (serviceBusTopicName != null && !serviceBusTopicName.isEmpty()) {
            serviceBusSenderClient.topicName(serviceBusTopicName);
            log.info("Configuration for ServiceBusProducerConfig for the topic '{}' created successfully.", serviceBusTopicName);
        } else {
            serviceBusSenderClient.queueName(serviceBusQueueName);
            log.info("Configuration for ServiceBusProducerConfig for the queue '{}' created successfully.", serviceBusQueueName);
        }

        return serviceBusSenderClient.buildClient();

    }
}
