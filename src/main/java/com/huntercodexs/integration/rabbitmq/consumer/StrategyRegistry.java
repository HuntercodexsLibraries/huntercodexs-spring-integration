package com.huntercodexs.integration.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StrategyRegistry {

    private final List<RabbitConsumerStrategy> strategies;

    public RabbitConsumerStrategy getByStrategyName(String name) {
        if (name == null) return null;
        return strategies.stream()
                .filter(s -> s.supports().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Map<String, RabbitConsumerStrategy> asMap() {
        return strategies.stream().collect(Collectors.toMap(RabbitConsumerStrategy::supports, s -> s));
    }
}
