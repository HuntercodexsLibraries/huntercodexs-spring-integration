package com.huntercodexs.integration;

import com.huntercodexs.integration.openfeign.intercept.FeignClientInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class FeignClientInterceptorConfig implements FeignClientInterceptor {

    private String jwtToken = "Bearer tokenValue";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String APPLICATION_JSON = "application/json";

    private static final String ORGANIZATIONAL_API = "organizational";
    private static final String USER_MANAGER_API = "user-manager";

    @Autowired
    private OrganizationalService organizationalService;
    @Autowired
    private UserManagerService userManagerService;

    @Override
    public void apply(RequestTemplate requestTemplate) {

        if (Objects.equals(ORGANIZATIONAL_API, requestTemplate.feignTarget().name())) {
            retrieveClientToken(requestTemplate, ORGANIZATIONAL_API);
        }

        if (Objects.equals(USER_MANAGER_API, requestTemplate.feignTarget().name())) {
            retrieveClientToken(requestTemplate, USER_MANAGER_API);
        }

        if (isEmpty(requestTemplate.body()) && checkRequestMethod(requestTemplate.method())) {
            requestTemplate.body("{}");
        }
    }

    @Override
    public boolean checkRequestMethod(String method) {
        return !method.equalsIgnoreCase(GET.toString());
    }

    @Override
    public void retrieveClientToken(RequestTemplate requestTemplate, String target) {
        requestTemplate.header(CONTENT_TYPE_HEADER, APPLICATION_JSON);

        if (Objects.equals(ORGANIZATIONAL_API, requestTemplate.feignTarget().name())) {
            organizationalService.getClientToken().ifPresent(token -> jwtToken = token);
        }

        if (Objects.equals(USER_MANAGER_API, requestTemplate.feignTarget().name())) {
            userManagerService.getClientToken().ifPresent(token -> jwtToken = token);
        }

        requestTemplate.header(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        requestTemplate.header(AUTHORIZATION_HEADER, jwtToken);
        System.out.println("HEADER: " + requestTemplate.headers());
    }

    @Service
    public static class OrganizationalService {
        public Optional<String> getClientToken() {
            return Optional.of("Bearer OrganizationTokenFake");
        }
    }

    @Service
    public static class UserManagerService {
        public Optional<String> getClientToken() {
            System.out.println("calling getClientToken from UserManagerService");
            return Optional.of("Bearer UserManagerTokenFake");
        }
    }
}
