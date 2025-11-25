package com.huntercodexs.integration.core.config;

import com.huntercodexs.integration.core.decoder.IntegrationErrorDecoder;
import com.huntercodexs.integration.core.interfaces.IntegrationRetryInterceptor;
import com.huntercodexs.integration.core.retry.IntegrationLoggerRetry;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static com.huntercodexs.integration.core.constants.IntegrationCoreConstants.CORE_LOGGING_APP_CONFIG;
import static com.huntercodexs.integration.core.constants.IntegrationCoreConstants.CORE_RETRYER_APP_CONFIG;

@Configuration
public class IntegrationClientConfig {

    @Value("${"+ CORE_RETRYER_APP_CONFIG +".period:1000}")
    private long period;

    @Value("${"+ CORE_RETRYER_APP_CONFIG +".max-period:1000}")
    private long maxPeriod;

    @Value("${"+ CORE_RETRYER_APP_CONFIG +".max-attempts:3}")
    private int maxAttempts;

    @Value("${"+ CORE_LOGGING_APP_CONFIG +".enabled:false}")
    private boolean logOn;

    @Bean
    @Primary
    public ErrorDecoder defaultErrorDecoder() {
        return new IntegrationErrorDecoder();
    }

    @Bean
    @Primary
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @Primary
    public Retryer retryer(List<IntegrationRetryInterceptor> interceptors) {
        return new IntegrationLoggerRetry(this.period, this.maxPeriod, this.maxAttempts, this.logOn, interceptors);
    }

}
