package com.huntercodexs.integration.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import static com.huntercodexs.integration.constants.IntegrationConstants.MONGO_DB_RETRYER_APP_CONFIG;

@Component
public class RetryTemplateConfig {

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".maxAttempts:3}")
    int maxAttempts;

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".initialInterval:2000}")
    long initialInterval;

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".maxInterval:10000}")
    long maxInterval;

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".multiplier:2.0}")
    double multiplier;

    public RetryTemplate mongoRetry() {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(maxAttempts);

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(initialInterval);
        backOff.setMaxInterval(maxInterval);
        backOff.setMultiplier(multiplier);

        template.setRetryPolicy(policy);
        template.setBackOffPolicy(backOff);

        return template;
    }
}
