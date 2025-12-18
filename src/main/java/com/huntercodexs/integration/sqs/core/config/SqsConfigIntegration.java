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
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

@Configuration
@ConditionalOnProperty(prefix = GLOBAL_BASE_CONFIG+".sqs", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SqsConfigIntegration {

    private static final Logger log = LoggerFactory.getLogger(SqsConfigIntegration.class);

    private static final String QUEUE_NAME_PROPERTY = "cloud.aws.queue.name";

    @Value("${cloud.aws.region.static:}")
    private String region;

    @Value("${cloud.aws.credentials.accessKey:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey:}")
    private String secretKey;

    @Value("${cloud.aws.endpoint.uri:}")
    private String endpointUri;

    @Value("${cloud.aws.sqs-client.maxAttempts:3}")
    private int maxAttempts;

    @Bean
    public SqsTemplate sqsTemplate() {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient()).build();
    }

    @Bean
    @Primary
    public SqsAsyncClient sqsAsyncClient() {

        URI endpoint = (endpointUri != null && !endpointUri.isEmpty())
                ? URI.create(endpointUri)
                : URI.create("https://sqs." + region + ".amazonaws.com");

        StaticCredentialsProvider credentials =
                StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                );

        RetryPolicy retryPolicy = RetryPolicy.builder()
                .numRetries(maxAttempts)
                .backoffStrategy(BackoffStrategy.defaultStrategy())
                .build();

        SqsAsyncClient sqsClient = SqsAsyncClient.builder()
                .credentialsProvider(credentials)
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder()
                                .retryPolicy(retryPolicy)
                                .build()
                )
                .build();

        log.info("Sqs Client was configured successfully: {}", sqsClient);

        return sqsClient;
    }

    @Bean(name = "dynamicSqsQueuesConsumer")
    public List<String> dynamicSqsQueuesConsumer(
            @Value("${"+QUEUE_NAME_PROPERTY+":}") String queueNamesProperty
    ) {
        if (queueNamesProperty == null || queueNamesProperty.trim().isEmpty()) {
            log.warn("No SQS queues configured in '{}'", QUEUE_NAME_PROPERTY);
            return List.of();
        }

        List<String> queues = Arrays.stream(queueNamesProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        log.info("Dynamic SQS queues loaded successfully: {}", queues);
        return queues;
    }
}
