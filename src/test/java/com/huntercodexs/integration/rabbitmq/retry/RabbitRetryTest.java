package com.huntercodexs.integration.rabbitmq.retry;

import com.huntercodexs.integration.rabbitmq.core.retry.RabbitLoggingListenerRetry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitRetryTest {

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    void shouldRetryThreeTimes() {

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
        retryTemplate.registerListener(new RabbitLoggingListenerRetry(true));
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(3000); // milliseconds
        retryTemplate.setBackOffPolicy(backOffPolicy);

        doThrow(new AmqpConnectException(new RuntimeException("Boom")))
                .when(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class),
                        any(MessagePostProcessor.class)
                );

        Object payload = "Test Message";

        assertThrows(AmqpConnectException.class, () ->
                retryTemplate.execute(ctx -> {
                    rabbitTemplate.convertAndSend(
                            "ex",
                            "rk",
                            payload,
                            msg -> msg
                    );
                    return null;
                })
        );

        verify(rabbitTemplate, times(3))
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class),
                        any(MessagePostProcessor.class)
                );
    }
}

