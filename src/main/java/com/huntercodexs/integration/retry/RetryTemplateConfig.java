package com.huntercodexs.integration.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class RetryTemplateConfig {

    @Value("${huntercodexs-spring-integration.mongodb.retry.maxAttempts:3}")
    int maxAttempts;

    @Value("${huntercodexs-spring-integration.mongodb.retry.initialInterval:2000}")
    long initialInterval;

    @Value("${huntercodexs-spring-integration.mongodb.retry.maxInterval:10000}")
    long maxInterval;

    @Value("${huntercodexs-spring-integration.mongodb.retry.multiplier:2.0}")
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
