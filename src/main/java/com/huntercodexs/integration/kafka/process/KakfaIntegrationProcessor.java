package com.huntercodexs.integration.kafka.process;

import java.util.HashMap;

public interface KakfaIntegrationProcessor {
    boolean supports(String producerName);
    String processMessage(Object message);
    HashMap<String, String> producerRecord(Object message);
}
