package com.huntercodexs.integration.feignstarter.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class FeignStarterErroCustomizadoDecoder implements ErrorDecoder {
    public Exception decode(String methodKey, Response response) {
        ResponseStatusException var10000;
        switch (response.status()) {
            case 403:
                ResponseStatusException var8 = new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: " + response.reason());
                var10000 = var8;
                break;
            case 404:
                ResponseStatusException var7 = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource Not Found: " + response.reason());
                var10000 = var7;
                break;
            case 502:
                ResponseStatusException var6 = new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Bad Gateway: " + response.reason());
                var10000 = var6;
                break;
            case 503:
                ResponseStatusException var3 = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable: " + response.reason());
                var10000 = var3;
                break;
            default:
                ResponseStatusException var9;
                try {
                    var9 = new ResponseStatusException(HttpStatusCode.valueOf(response.status()), new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                var10000 = var9;
        }

        return var10000;
    }
}
