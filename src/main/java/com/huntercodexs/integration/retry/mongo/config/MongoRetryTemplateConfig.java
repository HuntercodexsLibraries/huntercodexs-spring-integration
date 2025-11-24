package com.huntercodexs.integration.retry.mongo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import static com.huntercodexs.integration.constants.IntegrationConstants.MONGO_DB_RETRYER_APP_CONFIG;

@Configuration
public class MongoRetryTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoRetryTemplateConfig.class);

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".enabled:true}")
    boolean mongoRetryEnabled;

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".maxAttempts:3}")
    int maxAttempts;

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".initialInterval:100}")
    long initialInterval;

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".maxInterval:1000}")
    long maxInterval;

    @Value("${"+MONGO_DB_RETRYER_APP_CONFIG+".multiplier:2.0}")
    double multiplier;

    public RetryTemplate mongoRetry() {

        if (!mongoRetryEnabled) {
            log.warn("MongoDB Retry Template is disabled.");
            return null;
        }

        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(maxAttempts);

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(initialInterval);
        backOff.setMaxInterval(maxInterval);
        backOff.setMultiplier(multiplier);

        template.setRetryPolicy(policy);
        template.setBackOffPolicy(backOff);

        log.info("MongoDB Retry Template configured with maxAttempts={}, initialInterval={}ms, maxInterval={}ms, multiplier={}",
                maxAttempts, initialInterval, maxInterval, multiplier);

        return template;
    }
}
