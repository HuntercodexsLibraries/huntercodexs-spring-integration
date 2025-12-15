package com.huntercodexs.integration.rabbitmq.core.handler;

import lombok.Getter;

@Getter
public class RabbitExceptionRouterIntegration extends RuntimeException {

    private final String target;

    public RabbitExceptionRouterIntegration(String message, String target) {
        super(message);
        this.target = target;
    }
}

