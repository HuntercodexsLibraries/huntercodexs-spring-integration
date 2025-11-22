package com.huntercodexs.integration.core.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class IntegrationErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        String body = "";
        int status = response == null ? -1 : response.status();

        if (response != null && response.body() != null) {
            try (java.io.InputStream is = response.body().asInputStream()) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
                if (body.isEmpty()) {
                    body = response.reason();
                }
            } catch (IOException e) {
                body = "Unable to read/decode body: " + e.getMessage();
            }
        }

        String msg = "Feign " + methodKey + " failed with status " + status + " : " + body;

        return new ResponseStatusException(HttpStatus.valueOf(status), msg);
    }

}
