package com.huntercodexs.integration.mongo.retry.constants;

import static com.huntercodexs.integration.constants.IntegrationConstants.GLOBAL_BASE_CONFIG;

public class IntegrationMongoConstants {

    public static final String MONGO_DB_SPRING_APP_CONFIG = "spring.data.mongodb";
    public static final String MONGO_DB_RETRYER_APP_CONFIG = GLOBAL_BASE_CONFIG + ".mongodb.retry";
    public static final String MONGO_DB_TIMEOUT_APP_CONFIG = GLOBAL_BASE_CONFIG + ".mongodb.timeout";

}
