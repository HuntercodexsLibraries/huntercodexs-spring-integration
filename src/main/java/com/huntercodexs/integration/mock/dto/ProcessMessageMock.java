package com.huntercodexs.integration.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMessageMock {
    private String id;
    private String userId; // Identifier for rate limiting
    private String content;
}
