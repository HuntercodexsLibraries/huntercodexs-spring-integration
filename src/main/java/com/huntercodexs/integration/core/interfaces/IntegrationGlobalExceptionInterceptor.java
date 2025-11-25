package com.huntercodexs.integration.core.interfaces;

import com.huntercodexs.integration.handler.enumerator.IntegrationGlobalEnum;

import java.util.List;

public interface IntegrationGlobalExceptionInterceptor {

    boolean supports(IntegrationGlobalEnum value);
    String message();
    String trackerId();
    String code();
    List<String> errors(Object exception);

}
