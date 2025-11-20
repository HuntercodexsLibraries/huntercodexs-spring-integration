package com.huntercodexs.integration.ratelimit.v2.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMessageV2 {
    private String id;
    private String userId;
    private String content;
}
