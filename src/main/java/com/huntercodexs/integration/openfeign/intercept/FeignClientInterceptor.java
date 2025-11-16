package com.huntercodexs.integration.openfeign.intercept;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public interface FeignClientInterceptor extends RequestInterceptor {

    @Override
    void apply(RequestTemplate requestTemplate);

    boolean checkRequestMethod(String method);

    void retrieveClientToken(RequestTemplate requestTemplate, String target);

}
