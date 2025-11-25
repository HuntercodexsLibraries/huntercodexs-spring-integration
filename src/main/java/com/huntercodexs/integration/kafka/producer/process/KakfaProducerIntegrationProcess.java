package com.huntercodexs.integration.kafka.producer.process;

import java.util.HashMap;

public interface KakfaProducerIntegrationProcess {
    boolean supports(String producerName);
    String processMessage(Object message);
    HashMap<String, String> producerRecord(Object message);
}
