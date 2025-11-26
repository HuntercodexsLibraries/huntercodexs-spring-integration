package com.huntercodexs.integration.kafka.producer.constants;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

public class KafkaProducerIntegrationConstants {

    public static final String KAFKA_PRODUCER_APP_CONFIG = GLOBAL_BASE_CONFIG + ".kafka.producer";
    public static final String KAFKA_PRODUCER_SPRING_APP_CONFIG = "spring.kafka.producer";
    public static final String SASL_PLAINTEXT_PROTOCOL = "SASL_PLAINTEXT";
    public static final String SASL_SSL_PROTOCOL = "SASL_SSL";
    public static final String PLAINTEXT_PROTOCOL = "PLAINTEXT";
    public static final String SSL_PROTOCOL = "SSL";
    public static final String TOPIC_DEFAULT = "kafka-topic-default";

}
