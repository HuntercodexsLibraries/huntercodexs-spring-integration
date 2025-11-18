package com.huntercodexs.integration.openfeign.starter.logger;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

public class FeignStarterLogHttp extends Logger {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FeignStarterLogHttp.class);

    protected void log(String s, String s1, Object... objects) {
    }

    protected void logRequest(String configKey, Logger.Level logLevel, Request request) {
        Map<String, String> cabecalhos = (Map)request.headers().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> String.join(",", (Iterable)entry.getValue())));
        String corpo = request.body() != null ? new String(request.body()) : "";
        String metodo = request.httpMethod().name();
        String url = request.url();
        log.info("Requisicao enviada - Metodo: {} | URL: {} | cabecalho: {} | corpo: {}", new Object[]{metodo, url, cabecalhos, corpo});
    }

    protected Response logAndRebufferResponse(String configKey, Logger.Level logLevel, Response response, long elapsedTime) throws IOException {
        Map<String, String> cabecalhos = (Map)response.headers().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> String.join(",", (Iterable)entry.getValue())));
        int status = response.status();
        String responseString = "";
        Response clonedResponse = response.toBuilder().build();
        if (Objects.nonNull(response.body())) {
            try {
                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                Response.Body responseBodyCopy = response.toBuilder().body(bodyData).build().body();
                clonedResponse = response.toBuilder().body(responseBodyCopy).build();
                responseString = Util.toString(responseBodyCopy.asReader(StandardCharsets.UTF_8));
            } catch (IOException exception) {
                log.error("Erro ao converter corpo de resposta recebida | motivo: {}", exception.getMessage());
            }
        }

        log.info("Requisicao recebida - Status: {} | Tempo de Resposta: {}ms | cabecalho: {} | corpo: {}", new Object[]{status, elapsedTime, cabecalhos, responseString});
        return clonedResponse;
    }
}
