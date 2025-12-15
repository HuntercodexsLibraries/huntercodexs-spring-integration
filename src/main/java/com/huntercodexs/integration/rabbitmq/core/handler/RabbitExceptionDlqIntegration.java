package com.huntercodexs.integration.rabbitmq.core.handler;

import lombok.Getter;

@Getter
public class RabbitExceptionDlqIntegration extends RuntimeException {

    public RabbitExceptionDlqIntegration(String message) {
        super(message);
    }
}

