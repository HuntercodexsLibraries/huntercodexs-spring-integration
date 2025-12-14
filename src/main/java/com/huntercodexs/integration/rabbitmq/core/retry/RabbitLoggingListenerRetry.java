package com.huntercodexs.integration.rabbitmq.core.retry;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

@RequiredArgsConstructor
public class RabbitLoggingListenerRetry implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(RabbitLoggingListenerRetry.class);

    private final boolean logGlobalEnabled;

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        if (logGlobalEnabled) {
            log.info("Starting RabbitMQ message send with retry mechanism, retry count: {}", context.getRetryCount());
        }
        return true;
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (logGlobalEnabled) {
            log.warn("Retry attempt {} failed while sending RabbitMQ message. Cause: {}", context.getRetryCount(), throwable.getMessage());
        }
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable == null && context.getRetryCount() > 0 && logGlobalEnabled) {
            log.info("RabbitMQ message sent successfully after {} retries", context.getRetryCount());
        }
    }
}

