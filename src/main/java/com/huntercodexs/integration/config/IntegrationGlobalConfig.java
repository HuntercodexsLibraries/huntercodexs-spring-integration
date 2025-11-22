package com.huntercodexs.integration.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static com.huntercodexs.integration.constants.IntegrationConstants.COMPONENT_SCAN_BASE_PACKAGE;

@Configuration
@ComponentScan(basePackages = COMPONENT_SCAN_BASE_PACKAGE)
public class IntegrationGlobalConfig {
}
