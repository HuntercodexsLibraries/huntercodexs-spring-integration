package com.huntercodexs.integration.rabbitmq.core.dto;

import lombok.Data;

@Data
public class RabbitDefaultIntegrationDto {
    private boolean logEnabled;
    private String name;
    private String exchange;
    private String routingKey;
    private String logText;
    private Object logArgs;
}
