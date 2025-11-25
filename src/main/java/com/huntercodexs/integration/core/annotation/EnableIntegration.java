package com.huntercodexs.integration.core.annotation;

import com.huntercodexs.integration.config.IntegrationGlobalConfig;
import com.huntercodexs.integration.core.config.IntegrationClientConfig;
import com.huntercodexs.integration.core.config.IntegrationClientInterceptorConfig;
import com.huntercodexs.integration.core.logger.IntegrationHttpLogger;
import com.huntercodexs.integration.core.resource.IntegrationImportSelector;
import com.huntercodexs.integration.handler.GlobalExceptionHandler;
import com.huntercodexs.integration.kafka.consumer.config.KafkaConsumerIntegrationConfig;
import com.huntercodexs.integration.kafka.producer.config.KafkaProducerIntegrationConfig;
import com.huntercodexs.integration.kafka.consumer.filter.KafkaConsumerIntegrationFilter;
import com.huntercodexs.integration.kafka.producer.sender.KafkaIntegrationProducer;
import com.huntercodexs.integration.mongo.retry.MongoRetry;
import com.huntercodexs.integration.mongo.retry.config.MongoRetryTemplateConfig;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@EnableKafka
@EnableFeignClients
@Import({
        IntegrationClientConfig.class
        , IntegrationHttpLogger.class
        , IntegrationImportSelector.class
        , IntegrationGlobalConfig.class
        , GlobalExceptionHandler.class
        , IntegrationClientInterceptorConfig.class
        , MongoRetryTemplateConfig.class
        , MongoRetry.class
        , KafkaProducerIntegrationConfig.class
        , KafkaIntegrationProducer.class
        , KafkaConsumerIntegrationConfig.class
        , KafkaConsumerIntegrationFilter.class
})
public @interface EnableIntegration {
    String[] value();
}

