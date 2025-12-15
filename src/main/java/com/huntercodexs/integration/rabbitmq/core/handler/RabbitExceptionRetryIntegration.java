package com.huntercodexs.integration.rabbitmq.core.handler;

import lombok.Getter;

@Getter
public class RabbitExceptionRetryIntegration extends RuntimeException {

    public RabbitExceptionRetryIntegration(String message) {
        super(message);
    }
}

