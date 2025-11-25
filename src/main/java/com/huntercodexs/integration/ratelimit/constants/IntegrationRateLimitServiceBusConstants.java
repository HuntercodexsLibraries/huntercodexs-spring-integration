package com.huntercodexs.integration.ratelimit.constants;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

public class IntegrationRateLimitServiceBusConstants {

    public static final    int RATE_LIMIT_SERVICE_BUS_LIMIT_DEFAULT = 100;
    public static final    int RATE_LIMIT_SERVICE_BUS_DURATION_DEFAULT = 10;
    public static final String RATE_LIMIT_SERVICE_BUS_APP_CONFIG = GLOBAL_BASE_CONFIG + ".rate-limit-service-bus";
    public static final String RATE_LIMIT_SERVICE_BUS_LOG_APP_CONFIG = GLOBAL_BASE_CONFIG + ".rate-limit-service-bus.log";
    public static final String RATE_LIMIT_SERVICE_BUS_TIME_UNIT_SECONDS = "SECONDS";
    public static final String RATE_LIMIT_SERVICE_BUS_TIME_UNIT_MINUTES = "MINUTES";
    public static final String RATE_LIMIT_SERVICE_BUS_TIME_UNIT_HOURS = "HOURS";
    public static final String RATE_LIMIT_SERVICE_BUS_KEY_PARAMETER_NAME_DEFAULT = "__DEFAULT__";
    public static final String RATE_LIMIT_SERVICE_BUS_MSG_EXCEEDED = "Limit of %d requests exceeded for key '%s' in %d %s.";
    public static final String RATE_LIMIT_SERVICE_BUS_MSG_EXCEEDED_2 = "Rate Limit Service Bus exceeded to the key %s with limit of %d requests in %d %s.";

}
