package com.huntercodexs.integration.servicebus.producer.implement;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.huntercodexs.integration.servicebus.constants.ServiceBusIntegrationConstants.SERVICEBUS_SPRING_CLOUD_APP_CONFIG;

@Component
public class ServiceBusIntegrationProducer {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusIntegrationProducer.class);

    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;

    @Value("${"+SERVICEBUS_SPRING_CLOUD_APP_CONFIG+".producer.delay-minutes:}")
    private String delayMinutes;

    @Value("${"+SERVICEBUS_SPRING_CLOUD_APP_CONFIG+".producer.delay-seconds:}")
    private String delaySeconds;

    public ServiceBusIntegrationProducer(
            @Qualifier("serviceBusIntegrationProducerClient") ServiceBusSenderClient serviceBusSenderClient
    ) {
        this.objectMapper = new ObjectMapper();
        this.serviceBusSenderClient = serviceBusSenderClient;
    }

    @SneakyThrows
    public void send(Map<String, String> headers, long attempts, Object message, Class<?> clazz) {

        final Object payload = (clazz != null) ? objectMapper.convertValue(message, clazz) : message;
        final String mensagem = objectMapper.writeValueAsString(payload);
        final ServiceBusMessage serviceBusMensagem = new ServiceBusMessage(mensagem);

        OffsetDateTime time;
        serviceBusMensagem.getApplicationProperties().put("attempts", attempts);

        if (headers != null) {
            headers.forEach(serviceBusMensagem.getApplicationProperties()::put);
        }

        if (delaySeconds != null && !delaySeconds.isEmpty()) {
            time = OffsetDateTime.now().plusSeconds(Long.parseLong(delaySeconds));
            serviceBusMensagem.setScheduledEnqueueTime(time);
        } else if (delayMinutes != null && !delayMinutes.isEmpty()) {
            time = OffsetDateTime.now().plusMinutes(Long.parseLong(delayMinutes));
            serviceBusMensagem.setScheduledEnqueueTime(time);
        }

        log.info("Sending message to queue");
        serviceBusSenderClient.sendMessage(serviceBusMensagem);
        log.info("Message sent successfully to queue");

    }
}