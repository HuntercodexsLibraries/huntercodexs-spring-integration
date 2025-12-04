package com.huntercodexs.integration.core.decoder;

import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDecoderIntegrationTest {

    private final ErrorDecoderIntegration decoder = new ErrorDecoderIntegration();

    @Test
    void decode_ShouldThrowIllegalArgumentException_WhenResponseIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> decoder.decode("methodKeyNullResponse", null));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    void decode_ShouldReturnResponseStatusException_WhenBodyIsNull() {
        feign.Request request = feign.Request.create(
                "GET",
                "http://localhost/test",
                Map.of(),
                null,
                null
        );

        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .headers(Collections.emptyMap())
                .request(request)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> { throw decoder.decode("methodKeyBodyNull", response); });

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Feign methodKeyBodyNull failed with status 400"));
        assertTrue(ex.getReason().endsWith(" : "));
    }

    @Test
    void decode_ShouldUseReason_WhenBodyIsEmpty() {
        feign.Request request = feign.Request.create(
                "GET",
                "http://localhost/test",
                Map.of(),
                null,
                null
        );

        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .headers(Collections.emptyMap())
                .body(new byte[0])
                .request(request)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> { throw decoder.decode("methodKeyEmptyBody", response); });

        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Feign methodKeyEmptyBody failed with status 404"));
        assertTrue(ex.getReason().endsWith(" : Not Found"));
    }

    @Test
    void decode_ShouldHandleIOException_WhenReadingBodyFails() {
        Response.Body faultyBody = new Response.Body() {
            @Override
            public Integer length() { return null; }
            @Override
            public boolean isRepeatable() { return false; }
            public boolean isReplayable() { return false; }
            @Override
            public InputStream asInputStream() {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("boom");
                    }
                };
            }
            @Override
            public Reader asReader(Charset charset) { return null; }
            @Override
            public void close() { }
        };

        feign.Request request = feign.Request.create(
                "GET",
                "http://localhost/test",
                Map.of(),
                null,
                null
        );

        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .headers(Collections.emptyMap())
                .body(faultyBody)
                .request(request)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> { throw decoder.decode("methodKeyIOException", response); });

        assertEquals(500, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Unable to read/decode body:"));
        assertTrue(ex.getReason().contains("Feign methodKeyIOException failed with status 500"));
    }

    @Test
    void decode_ShouldUseBody_WhenBodyHasContent() {
        String content = "Error details";

        feign.Request request = feign.Request.create(
                "GET",
                "http://localhost/test",
                Map.of(),
                null,
                null
        );

        Response response = Response.builder()
                .status(502)
                .reason("Bad Gateway")
                .headers(Collections.emptyMap())
                .body(content.getBytes())
                .request(request)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> { throw decoder.decode("methodKeyBodyContent", response); });

        assertEquals(502, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Feign methodKeyBodyContent failed with status 502"));
        assertTrue(ex.getReason().endsWith(" : " + content));
    }

    @Test
    void decode_ShouldFallbackToNullReason_WhenBodyEmptyAndReasonNull() {
        feign.Request request = feign.Request.create(
                "GET",
                "http://localhost/test",
                Map.of(),
                null,
                null
        );

        Response response = Response.builder()
                .status(418)
                .reason(null)
                .headers(Map.of())
                .body(new byte[0])
                .request(request)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> { throw decoder.decode("methodKeyNullReason", response); });

        assertEquals(418, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Feign methodKeyNullReason failed with status 418"));
        assertTrue(ex.getReason().endsWith(" : null"));
    }

}