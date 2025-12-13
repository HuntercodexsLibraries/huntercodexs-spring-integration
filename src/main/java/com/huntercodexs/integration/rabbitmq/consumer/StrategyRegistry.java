package com.huntercodexs.integration.rabbitmq.consumer;

import com.huntercodexs.integration.rabbitmq.consumer.implement.RabbitConsumerStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StrategyRegistry {

    private final List<RabbitConsumerStrategy> strategies;

    public RabbitConsumerStrategy getByName(String name) {
        if (name == null) return null;
        return strategies.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // expose map if needed
    public Map<String, RabbitConsumerStrategy> asMap() {
        return strategies.stream().collect(Collectors.toMap(RabbitConsumerStrategy::getName, s -> s));
    }
}
