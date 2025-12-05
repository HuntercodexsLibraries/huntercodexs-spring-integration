package com.huntercodexs.integration.servicebus.consumer.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.huntercodexs.integration.servicebus.consumer.implement.ServiceBusProcessorIntegration;
import com.huntercodexs.integration.servicebus.consumer.implement.ServiceBusProcessorIntegrationDefaultImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.huntercodexs.integration.servicebus.constants.ServiceBusIntegrationConstants.SERVICEBUS_SPRING_CLOUD_APP_CONFIG;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.list.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceBusConsumerListConfig {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusConsumerListConfig.class);

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".connection-string}")
    private String connectionString;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.target-queue-list:}")
    private String serviceBusQueueList;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.receive-mode:PEEK_LOCK}")
    private String receiveModeConsumer;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.prefer-fetch-count:1}")
    private int preferFetchCount;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.max-concurrent-calls:1}")
    private int maxConcurrentCalls;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.max-auto-renew-duration-minutes:5}")
    private int maxAutoRenewDurationMinutes;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.disable-auto-complete:true}")
    private boolean disableAutoComplete;

    private final List<ServiceBusProcessorIntegration> processors;
    private final List<ServiceBusProcessorClient> clients = new ArrayList<>();
    private final ServiceBusProcessorIntegrationDefaultImpl serviceBusProcessorIntegrationDefault;

    @PostConstruct
    public void startProcess() {

        String[] queueNames = serviceBusQueueList.split(",");

        for (String queueName : queueNames) {

            ServiceBusProcessorIntegration processor = processors.stream()
                    .filter(p -> p.supports(queueName.trim()))
                    .findFirst()
                    .orElse(null);

            if (processor == null) {
                log.warn("No specific processor found for queue '{}', using default processor.", queueName);
                processor = serviceBusProcessorIntegrationDefault;
            }

            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .processor()
                    .queueName(queueName.trim())
                    .receiveMode(ServiceBusReceiveMode.valueOf(receiveModeConsumer))
                    .prefetchCount(preferFetchCount)
                    .maxConcurrentCalls(maxConcurrentCalls)
                    .processMessage(processor::processMessage)
                    .processError(processor::processError)
                    .maxAutoLockRenewDuration(Duration.ofMinutes(maxAutoRenewDurationMinutes));

            if (disableAutoComplete) {
                builder.disableAutoComplete();
                log.info("Auto-complete is disabled for Service Bus Consumer for List");
            } else {
                log.info("Auto-complete is enabled for Service Bus Consumer for List");
            }

            ServiceBusProcessorClient client = builder.buildProcessorClient();
            client.start();
            clients.add(client);

            log.info("Service Bus Consumer for List started for queue: {}", queueName);
        }
    }

    @PreDestroy
    public void stopProcess() {
        for (ServiceBusProcessorClient client : clients) {
            client.close();
        }
        log.info("All Service Bus Consumers for List have stopped successfully");
    }

}
