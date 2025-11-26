package com.huntercodexs.integration.kafka.consumer.constants;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

public class KafkaConsumerIntegrationConstants {

    public static final String KAFKA_CONSUMER_APP_CONFIG = GLOBAL_BASE_CONFIG + ".kafka.consumer";
    public static final String KAFKA_CONSUMER_SPRING_APP_CONFIG = "spring.kafka.consumer";
    public static final String SASL_PLAINTEXT_PROTOCOL = "SASL_PLAINTEXT";
    public static final String SASL_SSL_PROTOCOL = "SASL_SSL";
    public static final String PLAINTEXT_PROTOCOL = "PLAINTEXT";
    public static final String SSL_PROTOCOL = "SSL";
    public static final String TOPIC_DEFAULT = "kafka-topic-default";
    public static final String GROUP_ID_DEFAULT = "kafka-consumer-group-default";

}
