package com.huntercodexs.openfeign.resource;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpRetryException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        //return new HttpRetryException("Error occurred with status code: " + response.status(), response.status());

        String body = "";
        int status = response == null ? -1 : response.status();

        if (response != null && response.body() != null) {
            try (java.io.InputStream is = response.body().asInputStream()) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            } catch (IOException e) {
                body = "unable to read body: " + e.getMessage();
            }
        }

        String msg = "Feign " + s + " failed with status " + status + (body.isEmpty() ? "" : ": " + body);

        if (status >= 500) {
            return new HttpRetryException(msg, status);
        } else if (status == 404) {
            return new NoSuchElementException(msg);
        } else if (status >= 400) {
            return new IllegalArgumentException(msg);
        }
        return new Exception(msg);
    }

}
