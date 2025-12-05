package com.huntercodexs.integration.kafka.consumer.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConsumerCommonIntegrationConfigTest {

    static class TestConfig extends KafkaConsumerCommonIntegrationConfig {
        TestConfig() {
            // Permite instanciar a classe abstrata para teste
            super();
        }
    }

    TestConfig config;

    @BeforeEach
    void setUp() {
        config = new TestConfig();
        // Valores default
        config.securityProtocol = "SASL_PLAINTEXT";
        config.saslMechanism = "PLAIN";
        config.clusterApiKeyConsumer = "user";
        config.clusterApiSecretConsumer = "pass";
        config.truststoreLocation = "/tmp/truststore.jks";
        config.truststorePassword = "trustpass";
        config.disableHostnameVerification = false;
        config.clientCertificateRequired = false;
        config.keyPassword = "keypass";
    }

    @Test
    void testDefaultProtocolAndJaasConfig() {
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("SASL_PLAINTEXT", props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertEquals("PLAIN", props.get(SaslConfigs.SASL_MECHANISM));
        assertTrue(props.get(SaslConfigs.SASL_JAAS_CONFIG).toString().contains("username='user'"));
        assertTrue(props.get(SaslConfigs.SASL_JAAS_CONFIG).toString().contains("password='pass'"));
        assertFalse(props.containsKey(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
    }

    @Test
    void testInvalidProtocolDefaultsToSaslPlaintext() {
        config.securityProtocol = "INVALID";
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("SASL_PLAINTEXT", props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void testSaslSslWithTruststore() {
        config.securityProtocol = "SASL_SSL";
        config.truststoreLocation = "/tmp/truststore.jks";
        config.truststorePassword = "trustpass";
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("SASL_SSL", props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertEquals("/tmp/truststore.jks", props.get(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
        assertEquals("trustpass", props.get(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        assertEquals("JKS", props.get(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG));
    }

    @Test
    void testSaslSslWithoutTruststoreWarns() {
        config.securityProtocol = "SASL_SSL";
        config.truststoreLocation = "";
        config.truststorePassword = "";
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("SASL_SSL", props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertFalse(props.containsKey(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
    }

    @Test
    void testDisableHostnameVerification() {
        config.securityProtocol = "SASL_SSL";
        config.truststoreLocation = "/tmp/truststore.jks";
        config.truststorePassword = "trustpass";
        config.disableHostnameVerification = true;
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("", props.get(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
    }

    @Test
    void testClientCertificateRequired() {
        config.clientCertificateRequired = true;
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("/tmp/truststore.jks", props.get(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
        assertEquals("trustpass", props.get(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
        assertEquals("keypass", props.get(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
        assertEquals("JKS", props.get(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG));
    }
}