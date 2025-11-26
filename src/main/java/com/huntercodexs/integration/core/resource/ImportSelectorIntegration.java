package com.huntercodexs.integration.core.resource;

import com.huntercodexs.integration.core.annotation.EnableIntegration;
import com.huntercodexs.integration.core.config.AutoConfigIntegration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Objects;

public class ImportSelectorIntegration implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {

        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableIntegration.class.getName());

        String[] packages = (String[]) Objects.requireNonNull(attrs).get("value");

        PackageHolderIntegration.setBasePackages(packages);

        return new String[]{AutoConfigIntegration.class.getName()};
    }
}

