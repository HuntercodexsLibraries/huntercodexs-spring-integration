package com.huntercodexs.integration.core.interfaces;

import com.huntercodexs.integration.enumerator.IntegrationGlobalEnum;

import java.util.List;

public interface IntegrationGlobalExceptionInterceptor {

    boolean supports(IntegrationGlobalEnum value);
    String message();
    String trackerId();
    List<String> errors();

}
