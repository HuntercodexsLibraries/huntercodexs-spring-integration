package com.huntercodexs.integration.kafka.consumer.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.huntercodexs.integration.kafka.consumer.constants.KafkaConsumerIntegrationConstants.KAFKA_CONSUMER_APP_CONFIG;
import static com.huntercodexs.integration.kafka.consumer.constants.KafkaConsumerIntegrationConstants.KAFKA_CONSUMER_SPRING_APP_CONFIG;

@Configuration
public abstract class KafkaConsumerCommonIntegrationConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerCommonIntegrationConfig.class);

    @Value("${"+ KAFKA_CONSUMER_APP_CONFIG +".enabled:true}")
    protected boolean kafkaConsumerEnabled;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".cluster-key}")
    protected String clusterApiKeyConsumer;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".cluster-password}")
    protected String clusterApiSecretConsumer;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".security.protocol:SASL_PLAINTEXT}")
    protected String securityProtocol;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".sasl.mechanism:PLAIN}")
    protected String saslMechanism;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".ssl.truststore.location:}")
    protected String truststoreLocation;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".ssl.truststore.password:}")
    protected String truststorePassword;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".ssl.disable-hostname-verification:false}")
    protected boolean disableHostnameVerification;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".client-requires-trust-key:false}")
    protected boolean clientCertificateRequired;

    @Value("${"+ KAFKA_CONSUMER_SPRING_APP_CONFIG +".ssl.key-password:}")
    protected String keyPassword;

    @Value("${"+KAFKA_CONSUMER_SPRING_APP_CONFIG+".bootstrap-servers}")
    protected String bootstrapConsumerServer;

    @Value("${"+KAFKA_CONSUMER_SPRING_APP_CONFIG+".group-id}")
    protected String groupId;

    @Value("${"+KAFKA_CONSUMER_SPRING_APP_CONFIG+".max-poll-interval-ms:600000}")
    protected String maxPollIntervalMs;

    @Value("${"+KAFKA_CONSUMER_SPRING_APP_CONFIG+".max-poll-records:120}")
    protected String maxPollRecords;

    protected Map<String, Object> commonKafkaProps() {
        Map<String, Object> props = new HashMap<>();

        String protocol = securityProtocol == null ? "SASL_PLAINTEXT" : securityProtocol.trim().toUpperCase();

        if (!protocol.equals("SASL_PLAINTEXT") && !protocol.equals("SASL_SSL")) {
            protocol = "SASL_PLAINTEXT";
            log.warn("Invalid protocol configured. Defaulting to SASL_PLAINTEXT.");
        }

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, protocol);
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);

        props.put(SaslConfigs.SASL_JAAS_CONFIG,
                String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",
                        clusterApiKeyConsumer, clusterApiSecretConsumer));

        if ("SASL_SSL".equals(protocol)) {
            if (!truststoreLocation.isBlank() && !truststorePassword.isBlank()) {
                props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
                props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
                props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
            } else {
                log.warn("Missing truststore configuration for SASL_SSL protocol.");
            }

            // Disable hostname verification if needed
            if (disableHostnameVerification) {
                props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            }
        }

        // If the broker requires client certificate (mTLS)
        if (clientCertificateRequired) {
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, truststoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, truststorePassword);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "JKS");
        }

        return props;
    }
}
