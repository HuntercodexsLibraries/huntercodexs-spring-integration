package com.huntercodexs.integration.kafka.producer.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.huntercodexs.integration.kafka.producer.constants.KafkaProducerIntegrationConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class KafkaProducerCommonIntegrationConfigTest {

    static class TestKafkaProducerCommonIntegrationConfig extends KafkaProducerCommonIntegrationConfig {
        public TestKafkaProducerCommonIntegrationConfig() {
            this.kafkaProducerEnabled = true;
            this.bootstrapProducerServer = "localhost:9092";
            this.clusterApiKeyProducer = "user";
            this.clusterApiSecretProducer = "pass";
            this.securityProtocol = SASL_PLAINTEXT_PROTOCOL;
            this.saslMechanism = "PLAIN";
            this.truststoreLocation = "/tmp/truststore.jks";
            this.truststorePassword = "trustpass";
            this.disableHostnameVerification = false;
            this.clientCertificateRequired = false;
            this.keyPassword = "keypass";
        }
        public void setSecurityProtocol(String protocol) { this.securityProtocol = protocol; }
        public void setTruststoreLocation(String loc) { this.truststoreLocation = loc; }
        public void setTruststorePassword(String pwd) { this.truststorePassword = pwd; }
        public void setDisableHostnameVerification(boolean disable) { this.disableHostnameVerification = disable; }
        public void setClientCertificateRequired(boolean required) { this.clientCertificateRequired = required; }
        public void setKeyPassword(String pwd) { this.keyPassword = pwd; }
        public void setSaslMechanism(String mech) { this.saslMechanism = mech; }
    }

    TestKafkaProducerCommonIntegrationConfig config;

    @BeforeEach
    void setUp() {
        config = new TestKafkaProducerCommonIntegrationConfig();
    }

    @Test
    void deveUsarSaslPlaintextPorPadraoEConfigurarJaas() {
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals(SASL_PLAINTEXT_PROTOCOL, props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertEquals("PLAIN", props.get(SaslConfigs.SASL_MECHANISM));
        String jaas = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertTrue(jaas.contains("username='user'"));
        assertTrue(jaas.contains("password='pass'"));
    }

    @Test
    void deveNormalizarENullParaSaslPlaintext() {
        config.setSecurityProtocol(null);
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals(SASL_PLAINTEXT_PROTOCOL, props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void protocoloInvalidoDeveCairEmSaslPlaintext() {
        config.setSecurityProtocol("invalid");
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals(SASL_PLAINTEXT_PROTOCOL, props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void saslSslComTruststoreConfigurado() {
        config.setSecurityProtocol(SASL_SSL_PROTOCOL);
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("/tmp/truststore.jks", props.get(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
        assertEquals("trustpass", props.get(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        assertEquals("JKS", props.get(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG));
        assertFalse(props.containsKey(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
    }

    @Test
    void saslSslSemTruststoreNaoAdicionaConfigsDeTruststore() {
        config.setSecurityProtocol(SASL_SSL_PROTOCOL);
        config.setTruststoreLocation("");
        config.setTruststorePassword("");
        Map<String, Object> props = config.commonKafkaProps();
        assertFalse(props.containsKey(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
        assertFalse(props.containsKey(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        assertFalse(props.containsKey(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG));
    }

    @Test
    void saslSslComHostnameVerificationDesabilitado() {
        config.setSecurityProtocol(SASL_SSL_PROTOCOL);
        config.setDisableHostnameVerification(true);
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("", props.get(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
    }

    @Test
    void requerCertificadoDoClienteMTLS() {
        config.setClientCertificateRequired(true);
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("/tmp/truststore.jks", props.get(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
        assertEquals("trustpass", props.get(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
        assertEquals("keypass", props.get(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
        assertEquals("JKS", props.get(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG));
    }

    @Test
    void aceitaTodosOsProtocolosSuportados() {
        String[] protocols = {
                SASL_PLAINTEXT_PROTOCOL,
                SASL_SSL_PROTOCOL,
                PLAINTEXT_PROTOCOL,
                SSL_PROTOCOL
        };
        for (String protocol : protocols) {
            config.setSecurityProtocol(protocol);
            Map<String, Object> props = config.commonKafkaProps();
            assertEquals(protocol, props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        }
    }

    @Test
    void mecanismoSaslPersonalizadoEhRespeitado() {
        config.setSaslMechanism("SCRAM-SHA-256");
        Map<String, Object> props = config.commonKafkaProps();
        assertEquals("SCRAM-SHA-256", props.get(SaslConfigs.SASL_MECHANISM));
    }
}