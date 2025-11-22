package com.huntercodexs.integration.core.interfaces;

import java.util.Map;

public interface IntegrationClientInterceptor {

    boolean checkSupport(Object value);
    String getClientToken();

    default String getTokenType() {
        return null;
    }

    default Map<String, String> getHeaders() {
        return null;
    }

}
