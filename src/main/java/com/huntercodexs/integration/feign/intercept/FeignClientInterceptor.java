package com.huntercodexs.integration.feign.intercept;

import java.util.Map;

public interface FeignClientInterceptor {

    boolean checkSupport(Object value);
    String getClientToken();

    default String getTokenType() {
        return null;
    }

    default Map<String, String> getHeaders() {
        return null;
    }

}
