package com.huntercodexs.integration.constants;

public class IntegrationConstants {

    private static final String DOMAIN_PACKAGE = "com";
    private static final String BASE_CONFIG = "huntercodexs.integration";

    public static final String LOGGING_APP_CONFIG = BASE_CONFIG + ".client.config.logging";
    public static final String PROXY_APP_CONFIG = BASE_CONFIG + ".client.config.proxy";
    public static final String RETRYER_APP_CONFIG = BASE_CONFIG + ".client.config.retryer";
    public static final String RATE_LIMIT_APP_CONFIG = BASE_CONFIG + ".rate-limit";
    public static final String RATE_LIMIT_SERVICE_BUS_APP_CONFIG = BASE_CONFIG + ".rate-limit-service-bus";
    public static final String RATE_LIMIT_SERVICE_BUS_LOG_APP_CONFIG = BASE_CONFIG + ".rate-limit-service-bus.log";
    public static final String REDIS_APP_CONFIG = BASE_CONFIG + ".redis";
    public static final String REDIS_SPRING_APP_CONFIG = "spring.data.redis";
    public static final String MONGO_DB_RETRYER_APP_CONFIG = BASE_CONFIG + ".mongodb.retry";

    public static final String COMPONENT_SCAN_BASE_PACKAGE = DOMAIN_PACKAGE +"."+ BASE_CONFIG;
    public static final String ENABLE_FEIGN_CLIENTS_BASE_PACKAGE_SPEL = "#{T("+COMPONENT_SCAN_BASE_PACKAGE+".core.resource.IntegrationPackageHolder).getBasePackages()}";

    public static final String TIME_UNIT_SECONDS = "SECONDS";
    public static final String TIME_UNIT_MINUTES = "MINUTES";
    public static final String TIME_UNIT_HOURS = "HOURS";

    public static final int LIMIT_RATE_LIMIT_DEFAULT = 100;
    public static final int DURATION_RATE_LIMIT_DEFAULT = 10;
    public static final String KEY_PARAMETER_NAME_DEFAULT = "__DEFAULT__";
    public static final String RETRYER_HANDLER_EXCEPTION_DEFAULT = "__DEFAULT__";

    public static final String MSG_RATE_LIMIT_EXCEEDED = "Limit of %d requests was exceeded in %d %s.";
    public static final String MSG_RATE_LIMIT_SERVICE_BUS_EXCEEDED = "Limit of %d requests exceeded for key '%s' in %d %s.";
    public static final String MSG_RATE_LIMIT_SERVICE_BUS_EXCEEDED_2 = "Rate Limit Service Bus exceeded to the key %s with limit of %d requests in %d %s.";

}
