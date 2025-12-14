package com.huntercodexs.integration.rabbitmq.core.util;

import com.huntercodexs.integration.rabbitmq.core.props.RabbitConsumersIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitProducersIntegrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitIntegrationUtil {

    private static final Logger log = LoggerFactory.getLogger(RabbitIntegrationUtil.class);

    private static final String SINGLE_RABBIT_FOUNDED_MSG = "==> Single RabbitMQ consumer properties detected (name, exchange, routingKey), " +
            "overriding 'consumers' list with a single entry. Is it intended?";

    public static boolean checkSingleConsumerPropertiesSet(RabbitConsumersIntegrationProperties consumersProperties, boolean log) {
        boolean result = (!consumersProperties.getName().isEmpty()) && !consumersProperties.getExchange().isEmpty() && !consumersProperties.getRoutingKey().isEmpty();

        if (result && log) {
            doLog(consumersProperties, SINGLE_RABBIT_FOUNDED_MSG, null);
        }

        return result;
    }

    public static void doLog(RabbitConsumersIntegrationProperties consumersProperties, String text, Object args) {
        if (!consumersProperties.isLogEnabled()) return;

        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];

        String fullClassName = caller.getClassName();
        String className = fullClassName.substring(
                fullClassName.lastIndexOf('.') + 1
        );

        text = className + "." + caller.getMethodName() + " - " + text;

        if (args != null && text.contains("{}")) {
            log.info(text, args);
        } else {
            log.info(text);
        }
    }

    public static void doLog(RabbitProducersIntegrationProperties producersProperties, String text, Object args) {
        if (!producersProperties.isLogEnabled()) return;

        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];

        String fullClassName = caller.getClassName();
        String className = fullClassName.substring(
                fullClassName.lastIndexOf('.') + 1
        );

        text = className + "." + caller.getMethodName() + " - " + text;

        if (args != null && text.contains("{}")) {
            log.info(text, args);
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
