package com.huntercodexs.integration.core.annotation;

import com.huntercodexs.integration.config.IntegrationGlobalConfig;
import com.huntercodexs.integration.core.config.IntegrationClientConfig;
import com.huntercodexs.integration.core.config.IntegrationClientInterceptorConfig;
import com.huntercodexs.integration.core.logger.IntegrationHttpLogger;
import com.huntercodexs.integration.core.resource.IntegrationImportSelector;
import com.huntercodexs.integration.handler.GlobalExceptionHandler;
import com.huntercodexs.integration.retry.mongo.MongoRetry;
import com.huntercodexs.integration.retry.mongo.config.MongoRetryTemplateConfig;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
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
})
public @interface EnableIntegration {
    String[] value();
}

