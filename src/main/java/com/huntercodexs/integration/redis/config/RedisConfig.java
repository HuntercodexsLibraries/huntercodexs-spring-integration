package com.huntercodexs.integration.redis.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static com.huntercodexs.integration.redis.constants.RedisIntegrationConstants.REDIS_APP_CONFIG;
import static com.huntercodexs.integration.redis.constants.RedisIntegrationConstants.REDIS_SPRING_APP_CONFIG;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${"+REDIS_SPRING_APP_CONFIG+".host}")
    private String redisHost;

    @Value("${"+REDIS_SPRING_APP_CONFIG+".port}")
    private int redisPort;

    @Value("${"+REDIS_SPRING_APP_CONFIG+".password}")
    private String redisPassword;

    @Value("${"+REDIS_SPRING_APP_CONFIG+".timeout}")
    private  int redisTimeout;

    @Value("${"+REDIS_SPRING_APP_CONFIG+".ssl.enabled}")
    private  boolean redisSsl;

    @Value("${"+REDIS_APP_CONFIG+".enabled:true}")
    private boolean redisOn;

    @Value("${"+REDIS_APP_CONFIG+".log.enabled:true}")
    private boolean redisLogOn;

    @Bean
    public RedisTemplate<String, Long> redisTemplate(LettuceConnectionFactory connectionFactory) {
        if (!redisOn) {
            log.warn("Redis is disabled. RedisTemplate bean will not be created.");
            return null;
        }

        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        if (!redisOn) return null;

        if (redisLogOn) {
            log.info("RedisConfig: HOST: {}", this.redisHost);
            log.info("RedisConfig: PORT: {}", this.redisPort);
            log.info("RedisConfig: TIMEOUT: {}", this.redisTimeout);
            log.info("RedisConfig: SSL: {}", this.redisSsl);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();

        if (this.redisSsl) builder = builder.useSsl().disablePeerVerification().and();

        ClientOptions clientOptions = ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .sslOptions(SslOptions.builder().jdkSslProvider().build())
                .build();

        builder.clientOptions(clientOptions);

        return this.buildConnectionFactory(builder);
    }

    private LettuceConnectionFactory buildConnectionFactory(
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        var connectionFactory = new LettuceConnectionFactory(this.buildRedisConfiguration(),
                this.buildClientConfig(builder));
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    private RedisStandaloneConfiguration buildRedisConfiguration() {
        var redisConfig = new RedisStandaloneConfiguration(this.redisHost, this.redisPort);
        redisConfig.setPassword(this.redisPassword);
        return redisConfig;
    }

    private LettuceClientConfiguration buildClientConfig(
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        return builder.commandTimeout(Duration.ofMillis(this.redisTimeout)).shutdownTimeout(Duration.ZERO).build();
    }
}

