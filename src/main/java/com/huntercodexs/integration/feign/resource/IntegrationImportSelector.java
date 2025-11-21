package com.huntercodexs.integration.feign.resource;

import com.huntercodexs.integration.feign.annotation.IntegrationEnable;
import com.huntercodexs.integration.feign.config.FeignAutoConfig;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Objects;

public class IntegrationImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {

        Map<String, Object> attrs = metadata.getAnnotationAttributes(IntegrationEnable.class.getName());

        String[] packages = (String[]) Objects.requireNonNull(attrs).get("value");

        IntegrationPackageHolder.setBasePackages(packages);

        return new String[]{FeignAutoConfig.class.getName()};
    }
}

