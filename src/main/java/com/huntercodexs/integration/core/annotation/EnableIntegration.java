package com.huntercodexs.integration.core.annotation;

import com.huntercodexs.integration.config.IntegrationConfig;
import com.huntercodexs.integration.core.config.ClientConfigIntegration;
import com.huntercodexs.integration.core.config.ClientInterceptorConfigIntegration;
import com.huntercodexs.integration.core.logger.HttpLoggerIntegration;
import com.huntercodexs.integration.core.resource.ImportSelectorIntegration;
import com.huntercodexs.integration.handler.GlobalExceptionHandler;
import com.huntercodexs.integration.kafka.consumer.config.KafkaConsumerIntegrationConfig;
import com.huntercodexs.integration.kafka.consumer.filter.KafkaConsumerIntegrationFilter;
import com.huntercodexs.integration.kafka.producer.config.KafkaProducerIntegrationConfig;
import com.huntercodexs.integration.kafka.producer.sender.KafkaProducerIntegration;
import com.huntercodexs.integration.mongo.retry.MongoRetry;
import com.huntercodexs.integration.mongo.retry.config.MongoRetryTemplateConfig;
import com.huntercodexs.integration.rabbitmq.producer.RabbitProducerIntegration;
import com.huntercodexs.integration.servicebus.consumer.config.ServiceBusConsumerListConfig;
import com.huntercodexs.integration.servicebus.consumer.config.ServiceBusConsumerSingleConfig;
import com.huntercodexs.integration.servicebus.producer.config.ServiceBusProducerConfig;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@EnableFeignClients
@Import({
        ClientConfigIntegration.class
        , HttpLoggerIntegration.class
        , ImportSelectorIntegration.class
        , IntegrationConfig.class
        , GlobalExceptionHandler.class
        , ClientInterceptorConfigIntegration.class
        , MongoRetryTemplateConfig.class
        , MongoRetry.class
        , KafkaProducerIntegrationConfig.class
        , KafkaProducerIntegration.class
        , KafkaConsumerIntegrationConfig.class
        , KafkaConsumerIntegrationFilter.class
        , ServiceBusConsumerListConfig.class
        , ServiceBusConsumerSingleConfig.class
        , ServiceBusProducerConfig.class
        , RabbitProducerIntegration.class
})
public @interface EnableIntegration {
    String[] value();
}

