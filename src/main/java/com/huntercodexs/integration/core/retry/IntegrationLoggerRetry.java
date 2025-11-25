package com.huntercodexs.integration.core.retry;

import com.huntercodexs.integration.core.interfaces.IntegrationRetryInterceptor;
import com.huntercodexs.integration.handler.exception.IntegrationRetryAttemptsExceededException;
import feign.RetryableException;
import feign.Retryer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.huntercodexs.integration.core.constants.IntegrationCoreConstants.CORE_RETRYER_HANDLER_EXCEPTION_DEFAULT;

@RequiredArgsConstructor
public class IntegrationLoggerRetry implements Retryer {

    private static final Logger log = LoggerFactory.getLogger(IntegrationLoggerRetry.class);

    private int attempt = 0;
    private long nextInterval = 1;

    private final long basePeriod;
    private final long maxPeriod;
    private final int maxAttempts;
    private final boolean logOn;
    private final List<IntegrationRetryInterceptor> interceptors;

    @Override
    public void continueOrPropagate(RetryableException e) {

        attempt++;

        if (attempt >= maxAttempts) {

            log.warn("Limit of retries reached, (tries: {}) | method: {} | url: {} | message: {}",
                    attempt,
                    e.method(),
                    e.request().url(),
                    e.getMessage());

            IntegrationRetryInterceptor retryInterceptor = interceptors.stream()
                    .filter(r -> r.supports(CORE_RETRYER_HANDLER_EXCEPTION_DEFAULT))
                    .findFirst()
                    .orElse(null);

            if (retryInterceptor != null) {
                retryInterceptor.execute();
                return;
            }

            throw new IntegrationRetryAttemptsExceededException("Integration Retries Exceeded: " + attempt, e);

        }

        if (logOn) {
            log.info("Retrying request - {}/{} | method: {} | url: {} | message: {}",
                    attempt,
                    maxAttempts,
                    e.method(),
                    e.request().url(),
                    e.getMessage());
        }

        nextInterval = nextInterval + Math.min(basePeriod * 2, maxPeriod);

        try {
            Thread.sleep(nextInterval);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();

            log.warn("Thread interrupted during backoff");
        }
    }

    @Override
    public Retryer clone() {
        return new IntegrationLoggerRetry(basePeriod, maxPeriod, maxAttempts, logOn, interceptors);
    }
}
