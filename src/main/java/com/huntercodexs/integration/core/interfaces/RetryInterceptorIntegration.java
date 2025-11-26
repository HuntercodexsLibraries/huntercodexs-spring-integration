package com.huntercodexs.integration.core.interfaces;

public interface RetryInterceptorIntegration {

    boolean supports(Object value);
    void execute();

}
