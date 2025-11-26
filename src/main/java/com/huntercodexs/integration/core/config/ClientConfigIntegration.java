package com.huntercodexs.integration.core.config;

import com.huntercodexs.integration.core.decoder.ErrorDecoderIntegration;
import com.huntercodexs.integration.core.interfaces.RetryInterceptorIntegration;
import com.huntercodexs.integration.core.retry.RetryLoggerIntegration;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static com.huntercodexs.integration.core.constants.CoreIntegrationConstants.CORE_LOGGING_APP_CONFIG;
import static com.huntercodexs.integration.core.constants.CoreIntegrationConstants.CORE_RETRYER_APP_CONFIG;

@Configuration
public class ClientConfigIntegration {

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
        return new ErrorDecoderIntegration();
    }

    @Bean
    @Primary
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @Primary
    public Retryer retryer(List<RetryInterceptorIntegration> interceptors) {
        return new RetryLoggerIntegration(this.period, this.maxPeriod, this.maxAttempts, this.logOn, interceptors);
    }

}
