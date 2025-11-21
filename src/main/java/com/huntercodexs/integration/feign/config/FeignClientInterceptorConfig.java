package com.huntercodexs.integration.feign.config;

import com.huntercodexs.integration.feign.intercept.FeignClientInterceptor;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class FeignClientInterceptorConfig implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignClientInterceptorConfig.class);

    @Value("${huntercodexs-spring-integration.client.config.logging.enabled:false}")
    private boolean logOn;

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String APPLICATION_JSON = "application/json";

    private String interceptor;
    private final List<FeignClientInterceptor> interceptors;

    @Override
    public void apply(RequestTemplate requestTemplate) {

        if (requestTemplate == null || isEmpty(requestTemplate.feignTarget()) || isEmpty(requestTemplate.feignTarget().name())) {
            if (logOn) log.info("RequestTemplate or Feign Target is null or empty");
            return;
        }

        this.interceptor = requestTemplate.feignTarget().name();

        retrieveClientToken(requestTemplate);
    }

    private void retrieveClientToken(RequestTemplate requestTemplate) {
        requestTemplate.header(CONTENT_TYPE_HEADER, APPLICATION_JSON);

        FeignClientInterceptor strategy = interceptors.stream()
                .filter(interceptor -> interceptor.checkSupport(this.interceptor))
                .findFirst()
                .orElse(null);

        if (strategy == null) {
            if (logOn) log.info("No interceptor found for target: {}", this.interceptor);
            return;
        }

        String token = strategy.getClientToken();
        String tokenType = strategy.getTokenType();
        Map<String, String> headers = strategy.getHeaders();

        if (tokenType != null && !token.isEmpty()) {
            token = tokenType.concat(" ").concat(token.replaceAll("(Basic|Bearer) ", ""));
        }

        requestTemplate.header(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        requestTemplate.header(AUTHORIZATION_HEADER, token);

        if (!isEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestTemplate.header(entry.getKey(), entry.getValue());
            }
        }

        if (isEmpty(requestTemplate.body()) && isNotRequestMethodGet(requestTemplate.method())) {
            requestTemplate.body("{}");
        }

        if (logOn) log.info("Interceptor applied for target: {}, headers: {}", this.interceptor, requestTemplate.headers());
    }

    private boolean isNotRequestMethodGet(String method) {
        return !method.equalsIgnoreCase(GET.toString());
    }

}
