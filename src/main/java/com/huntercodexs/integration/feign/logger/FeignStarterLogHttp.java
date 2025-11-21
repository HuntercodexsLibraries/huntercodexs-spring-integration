package com.huntercodexs.integration.feign.logger;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class FeignStarterLogHttp extends Logger {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FeignStarterLogHttp.class);

    @Value("${huntercodexs-spring-integration.client.config.logging.enabled:false}")
    private boolean logOn;

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {

        Map<String, String> headers = request.headers()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));

        var url = request.url();
        var method = request.httpMethod().name();
        var body = request.body() != null ? new String(request.body()) : "";

        if (logOn) log.info("Request sent - {} {} {} {}", method, url, headers, body);

    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {

        Map<String, String> headers = response
                .headers()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, (entry) -> String.join(",", entry.getValue())));

        int status = response.status();
        String responseString = "";
        Response clonedResponse = response.toBuilder().build();

        if (Objects.nonNull(response.body())) {

            try {

                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                Response.Body responseBodyCopy = response.toBuilder().body(bodyData).build().body();
                clonedResponse = response.toBuilder().body(responseBodyCopy).build();
                responseString = Util.toString(responseBodyCopy.asReader(StandardCharsets.UTF_8));

            } catch (IOException e) {
                log.error("Error converting received response body | reason: {}", e.getMessage());
            }

        }

        if (logOn) {
            log.info("Request received - Status: {} | ElapsedTime: {}ms | headers: {} | body: {}", status, elapsedTime, headers, responseString);
        }

        return clonedResponse;
    }

    @Override
    protected void log(String s, String s1, Object... objects) {
        //implements code here
    }
}
