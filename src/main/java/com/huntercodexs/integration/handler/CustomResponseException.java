package com.huntercodexs.integration.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

@Setter
@Getter
public class CustomResponseException {

    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tracker;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> errors;

    public CustomResponseException(String message, String code, String tracker, List<String> errors) {
        this.message = message;
        this.tracker = tracker;

        if (Objects.equals(code, "0") || isNull(code) || code.isEmpty()) {
            this.code = null;
        } else {
            this.code = code;
        }

        this.errors = errors;
        if (isNull(errors) || errors.isEmpty()) {
            this.errors = null;
        }
    }

}

