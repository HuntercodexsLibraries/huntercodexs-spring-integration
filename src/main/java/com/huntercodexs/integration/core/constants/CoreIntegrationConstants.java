package com.huntercodexs.integration.core.constants;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;
import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_COMPONENT_SCAN_BASE_PACKAGE;

@SuppressWarnings("java:S1118")
public class CoreIntegrationConstants {

    public static final String CORE_ENABLE_FEIGN_CLIENTS_BASE_PACKAGE_SPEL = "#{T("+ GLOBAL_COMPONENT_SCAN_BASE_PACKAGE +".core.resource.IntegrationPackageHolder).getBasePackages()}";
    public static final String CORE_LOGGING_APP_CONFIG = GLOBAL_BASE_CONFIG + ".client.config.logging";
    public static final String CORE_PROXY_APP_CONFIG = GLOBAL_BASE_CONFIG + ".client.config.proxy";
    public static final String CORE_RETRYER_APP_CONFIG = GLOBAL_BASE_CONFIG + ".client.config.retryer";
    public static final String CORE_RETRYER_HANDLER_EXCEPTION_CUSTOM = "__CUSTOM__";

}
