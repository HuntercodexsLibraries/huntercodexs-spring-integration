package com.huntercodexs.integration.sqs.core.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

@Configuration
@ConditionalOnProperty(prefix = GLOBAL_BASE_CONFIG+".sqs", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SqsConfigIntegration {

    private static final Logger log = LoggerFactory.getLogger(SqsConfigIntegration.class);

    @Value("${cloud.aws.account-id:}")
    private String accountId;

    @Value("${cloud.aws.queue.name:}")
    private String queueName;

    @Value("${cloud.aws.region.static:}")
    private String region;

    @Value("${cloud.aws.credentials.accessKey:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey:}")
    private String secretKey;

    @Value("${cloud.aws.endpoint.uri:}")
    private String endpointUri;

    @Bean
    public SqsTemplate sqsTemplate() {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient()).build();
    }

    @Bean
    @Primary
    public SqsAsyncClient sqsAsyncClient() {

        String destination = "https://sqs."+region+".amazonaws.com/"+accountId+"/"+queueName;

        if (endpointUri != null && !endpointUri.isEmpty()) {
            destination = endpointUri+accountId+"/"+queueName;
        }

        StaticCredentialsProvider credentials = StaticCredentialsProvider
                .create(AwsBasicCredentials.create(accessKey, secretKey));

        SqsAsyncClient sqsClient = SqsAsyncClient.builder()
                .credentialsProvider(credentials)
                .endpointOverride(URI.create(destination))
                .region(Region.of(region))
                .build();

        log.info("Sqs Client was configured successfully: {}", sqsClient);

        return sqsClient;
    }
}
