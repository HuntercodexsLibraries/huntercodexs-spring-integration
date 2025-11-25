package com.huntercodexs.integration.kafka.consumer.process;

import org.apache.kafka.common.header.Headers;

public interface KakfaConsumerIntegrationProcess {
    boolean supports(Headers headers, Object value, Object key, int partition, long offset);
    boolean discard(Headers headers, Object value, Object key, int partition, long offset);
}
