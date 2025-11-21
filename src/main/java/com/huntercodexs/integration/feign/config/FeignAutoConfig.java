package com.huntercodexs.integration.feign.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(
        basePackages = "#{T(com.huntercodexs.integration.feign.resource.IntegrationPackageHolder).getBasePackages()}"
)
public class FeignAutoConfig {
}
