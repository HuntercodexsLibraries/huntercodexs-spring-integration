package com.huntercodexs.integration.ratelimit.constants;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

public class RateLimitIntegrationConstants {

    public static final    int RATE_LIMIT_LIMIT_DEFAULT = 100;
    public static final    int RATE_LIMIT_DURATION_DEFAULT = 10;
    public static final String RATE_LIMIT_APP_CONFIG = GLOBAL_BASE_CONFIG + ".rate-limit";
    public static final String RATE_LIMIT_TIME_UNIT_SECONDS = "SECONDS";
    public static final String RATE_LIMIT_TIME_UNIT_MINUTES = "MINUTES";
    public static final String RATE_LIMIT_TIME_UNIT_HOURS = "HOURS";
    public static final String RATE_LIMIT_MSG_EXCEEDED = "Limit of %d requests was exceeded in %d %s.";

}
