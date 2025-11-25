package com.huntercodexs.integration.core.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

import static com.huntercodexs.integration.core.constants.IntegrationCoreConstants.CORE_ENABLE_FEIGN_CLIENTS_BASE_PACKAGE_SPEL;

@Configuration
@EnableFeignClients(basePackages = CORE_ENABLE_FEIGN_CLIENTS_BASE_PACKAGE_SPEL)
public class IntegrationAutoConfig {
}
