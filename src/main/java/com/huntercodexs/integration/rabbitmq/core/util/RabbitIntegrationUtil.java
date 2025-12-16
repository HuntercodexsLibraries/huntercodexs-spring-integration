package com.huntercodexs.integration.rabbitmq.core.util;

import com.huntercodexs.integration.rabbitmq.core.dto.RabbitDefaultIntegrationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitIntegrationUtil {

    private static final Logger log = LoggerFactory.getLogger(RabbitIntegrationUtil.class);

    private static final String DEFAULT_RABBIT_FOUNDED_MSG = "==> Single RabbitMQ consumer properties detected (name, exchange, routingKey), " +
            "overriding 'consumers' list with a single entry. Is it intended?";

    private static final String LIST_RABBIT_FOUNDED_MSG = "==> List RabbitMQ consumers and producers detected";

    public static boolean defaultConsumerPropertiesCheck(RabbitDefaultIntegrationDto defaultIntegrationDto) {
        boolean result =
                !defaultIntegrationDto.getName().isBlank() &&
                !defaultIntegrationDto.getExchange().isBlank() &&
                !defaultIntegrationDto.getRoutingKey().isBlank();

        if (result) {
            defaultIntegrationDto.setLogText(DEFAULT_RABBIT_FOUNDED_MSG);
        } else {
            defaultIntegrationDto.setLogText(LIST_RABBIT_FOUNDED_MSG);
        }

        doLog(defaultIntegrationDto);

        return result;
    }

    public static void doLog(RabbitDefaultIntegrationDto defaultIntegrationDto) {
        if (!defaultIntegrationDto.isLogEnabled()) return;

        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];

        String fullClassName = caller.getClassName();
        String className = fullClassName.substring(
                fullClassName.lastIndexOf('.') + 1
        );

        String text = className + "." + caller.getMethodName() + " - " + defaultIntegrationDto.getLogText();

        if (defaultIntegrationDto.getLogArgs()!= null && text.contains("{}")) {
            log.info(text, defaultIntegrationDto.getLogArgs());
        } else {
            log.info(text);
        }
    }

    public static String stripPayload(String payload) {
        return payload
                .replaceAll("\n", "")
                .replaceAll("\\\\n", "")
                .replaceAll("\r", "")
                .replace("\\", "")
                .replaceAll("^\"", "")
                .replaceAll("\"$", "");
    }
}
