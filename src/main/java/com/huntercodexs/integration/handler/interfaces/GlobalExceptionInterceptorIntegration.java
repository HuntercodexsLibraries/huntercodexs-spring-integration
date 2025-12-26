package com.huntercodexs.integration.handler.interfaces;

import com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration;

import java.util.List;

public interface GlobalExceptionInterceptorIntegration {

    boolean supports(GlobalEnumIntegration value);
    String message();
    String trackerId();
    String code();
    List<String> errors(Object exception);

}
