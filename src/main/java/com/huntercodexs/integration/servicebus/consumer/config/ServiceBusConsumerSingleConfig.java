package com.huntercodexs.integration.servicebus.consumer.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.huntercodexs.integration.servicebus.consumer.implement.ServiceBusProcessorIntegrationDefaultImpl;
import com.huntercodexs.integration.servicebus.consumer.implement.ServiceBusProcessorIntegration;
import com.huntercodexs.integration.servicebus.context.ServiceBusErrorContextIntegration;
import com.huntercodexs.integration.servicebus.context.ServiceBusMessageContextIntegration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;

import static com.huntercodexs.integration.servicebus.constants.ServiceBusIntegrationConstants.SERVICEBUS_DEFAULT_CONSUMER_QUEUE_NAME;
import static com.huntercodexs.integration.servicebus.constants.ServiceBusIntegrationConstants.SERVICEBUS_SPRING_CLOUD_APP_CONFIG;

@Primary
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.single.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceBusConsumerSingleConfig {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusConsumerSingleConfig.class);

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".connection-string}")
    private String connectionString;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.target-queue-name:"+ SERVICEBUS_DEFAULT_CONSUMER_QUEUE_NAME +"}")
    private String serviceBusQueueName;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.receive-mode:PEEK_LOCK}")
    private String receiveModeConsumer;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.prefetch-count:1}")
    private int preferFetchCount;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.max-concurrent-calls:1}")
    private int maxConcurrentCalls;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.max-auto-renew-duration-minutes:5}")
    private int maxAutoRenewDurationMinutes;

    @Value("${"+ SERVICEBUS_SPRING_CLOUD_APP_CONFIG +".consumer.disable-auto-complete:true}")
    private boolean disableAutoComplete;

    private ServiceBusProcessorClient client;
    private final List<ServiceBusProcessorIntegration> processors;
    private final ServiceBusProcessorIntegrationDefaultImpl serviceBusProcessorIntegrationDefault;

    @PostConstruct
    public void startProcess() {
        client = this.serviceBusProcessorClient();
        client.start();
        log.info("Service Bus Consumers has started successfully");
    }

    @PreDestroy
    public void stopProcess() {
        if (client != null) {
            client.close();
        }
        log.info("Service Bus Consumers has stopped successfully");
    }

    public ServiceBusProcessorClient serviceBusProcessorClient() {

        ServiceBusProcessorIntegration processor = processors.stream()
                .filter(p -> p.supports(serviceBusQueueName))
                .findFirst()
                .orElse(null);

        if (processor == null) {
            log.warn("No specific processor found for queue '{}', using default processor.", serviceBusQueueName);
            processor = serviceBusProcessorIntegrationDefault;
        }

        final ServiceBusProcessorIntegration selectedProcessor = processor;

        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .queueName(serviceBusQueueName)
                .receiveMode(ServiceBusReceiveMode.valueOf(receiveModeConsumer))
                .prefetchCount(preferFetchCount)
                .maxConcurrentCalls(maxConcurrentCalls)

                .processMessage(ctx ->
                        selectedProcessor.processMessage(new ServiceBusMessageContextIntegration(ctx)))

                .processError(err ->
                        selectedProcessor.processError(new ServiceBusErrorContextIntegration(err)))

                .maxAutoLockRenewDuration(Duration.ofMinutes(maxAutoRenewDurationMinutes));

        if (disableAutoComplete) {
            builder.disableAutoComplete();
            log.info("Auto-complete is disabled for Service Bus Consumer");
        } else {
            log.info("Auto-complete is enabled for Service Bus Consumer");
        }

        return builder.buildProcessorClient();

    }

}
