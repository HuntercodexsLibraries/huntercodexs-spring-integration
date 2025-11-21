package com.huntercodexs.integration.feign.annotation;

import com.huntercodexs.integration.feign.config.FeignClientConfig;
import com.huntercodexs.integration.feign.logger.FeignStarterLogHttp;
import com.huntercodexs.integration.feign.resource.IntegrationImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({FeignClientConfig.class, FeignStarterLogHttp.class, IntegrationImportSelector.class})
public @interface IntegrationEnable {
    String[] value();
}
