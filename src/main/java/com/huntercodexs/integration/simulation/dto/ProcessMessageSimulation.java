package com.huntercodexs.integration.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMessageSimulation {
    private String id;
    private String userId; // Identifier for rate limiting
    private String content;
}
