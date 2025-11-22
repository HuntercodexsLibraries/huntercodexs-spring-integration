package com.huntercodexs.integration.core.interfaces;

public interface IntegrationRetryInterceptor {

    boolean supports(Object value);
    void execute();

}
