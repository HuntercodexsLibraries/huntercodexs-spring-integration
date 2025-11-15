package com.huntercodexs.openfeign.logger;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class FeignStarterLogHttp extends Logger {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FeignStarterLogHttp.class);

    @Value("${huntercodexs-spring-open-feign.client.config.logging:false}")
    private boolean enableLogging;

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {

        var url = request.url();
        var method = request.httpMethod().name();
        var body = request.body() != null ? new String(request.body()) : "";

        Map<String, String> headers = request.headers()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));

        if (enableLogging) log.info("Request sent - {} {} {} {}", method, url, headers, body);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {

        var body = "";
        var status = response.status();

        Map<String, String> headers = response.headers()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));

        try {

            if (response.body() != null) {
                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                //keep the original body request
                body = new String(bodyData);
            }

        } catch (Exception e) {
            if (enableLogging) log.warn("Missing body request content: {}", e.getMessage());
        }

        if (enableLogging) {
            log.info("Request received - Status: {} | ElapsedTime: {}ms | headers: {} | body: {}", status, elapsedTime, headers, body);
        }

        return response;
    }

    @Override
    protected void log(String s, String s1, Object... objects) {
        //implements code here
    }
}
