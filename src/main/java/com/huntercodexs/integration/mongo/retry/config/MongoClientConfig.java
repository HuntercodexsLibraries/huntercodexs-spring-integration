package com.huntercodexs.integration.mongo.retry.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.mongo.retry.constants.MongoRetryIntegrationConstants.MONGO_DB_SPRING_APP_CONFIG;
import static com.huntercodexs.integration.mongo.retry.constants.MongoRetryIntegrationConstants.MONGO_DB_TIMEOUT_APP_CONFIG;

@Configuration
public class MongoClientConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoClientConfig.class);

    @Value("${"+MONGO_DB_SPRING_APP_CONFIG+".uri}")
    private String mongoUri;

    @Value("${"+MONGO_DB_TIMEOUT_APP_CONFIG+".serverSelection:1000}")
    private int serverSelectionTimeoutMs;

    @Value("${"+MONGO_DB_TIMEOUT_APP_CONFIG+".connect:1000}")
    private int connectTimeoutMs;

    @Value("${"+MONGO_DB_TIMEOUT_APP_CONFIG+".socket:1000}")
    private int socketTimeoutMs;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString cs = new ConnectionString(mongoUri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .applyToClusterSettings(b -> b.serverSelectionTimeout(serverSelectionTimeoutMs, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(b -> b
                        .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                        .readTimeout(socketTimeoutMs, TimeUnit.MILLISECONDS))
                .retryWrites(true)
                .build();

        log.info("MongoClient configuration serverSelectionTimeout={}ms connectTimeout={}ms socketTimeout={}ms",
                serverSelectionTimeoutMs, connectTimeoutMs, socketTimeoutMs);

        return MongoClients.create(settings);
    }
}
