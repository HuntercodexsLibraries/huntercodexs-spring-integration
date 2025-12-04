package com.huntercodexs.integration.core.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static feign.Logger.Level.FULL;
import static org.junit.jupiter.api.Assertions.*;

class HttpLoggerIntegrationTest {

    private HttpLoggerIntegration loggerIntegration;
    private Logger slf4jLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setup() {
        loggerIntegration = new HttpLoggerIntegration();

        // Inject logOn via reflection to control logging
        setLogOn(true);

        // Attach a ListAppender to capture logs
        slf4jLogger = (Logger) LoggerFactory.getLogger(HttpLoggerIntegration.class);
        appender = new ListAppender<>();
        appender.start();
        // Attach to Logback logger and set level
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(HttpLoggerIntegration.class);
        logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
        logbackLogger.addAppender(appender);
    }

    private void setLogOn(boolean value) {
        try {
            var field = HttpLoggerIntegration.class.getDeclaredField("logOn");
            field.setAccessible(true);
            field.set(loggerIntegration, value);
        } catch (Exception e) {
            fail("Failed to set logOn: " + e.getMessage());
        }
    }

    @Test
    void logRequest_logs_whenEnabled_withBodyAndHeaders() {
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("application/json"));
        headers.put("X-Trace-Id", List.of("abc123"));

        byte[] body = "{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8);

        Request request = Request.create(
                Request.HttpMethod.POST,
                "http://localhost/api/test",
                headers,
                body,
                Util.UTF_8,
                null
        );

        loggerIntegration.logRequest("cfg", FULL, request);

        assertFalse(appender.list.isEmpty(), "Expected logs when enabled");
        String msg = appender.list.get(0).getFormattedMessage();
        assertTrue(msg.contains("Request sent - method: POST"));
        assertTrue(msg.contains("url: http://localhost/api/test"));
        assertTrue(msg.contains("Content-Type"));
        assertTrue(msg.contains("X-Trace-Id"));
        assertTrue(msg.contains("\"hello\":\"world\""));
    }

    @Test
    void logRequest_doesNotLog_whenDisabled() {
        setLogOn(false);

        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://localhost/no-log",
                Collections.emptyMap(),
                null,
                Util.UTF_8,
                null
        );

        loggerIntegration.logRequest("cfg", FULL, request);

        assertTrue(appender.list.isEmpty(), "No logs expected when disabled");
    }

    @Test
    void logAndRebufferResponse_logsAndClonesBody() throws IOException {
        setLogOn(true);

        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("application/json"));

        String payload = "{\"ok\":true}";
        Response response = Response.builder()
                .status(200)
                .reason("OK")
                .headers(headers)
                .request(Request.create(Request.HttpMethod.GET, "http://localhost/api", Collections.emptyMap(), null, Util.UTF_8, null))
                .body(payload, StandardCharsets.UTF_8)
                .build();

        Response cloned = loggerIntegration.logAndRebufferResponse("cfg", FULL, response, 123L);

        assertEquals(200, cloned.status());
        assertNotNull(cloned.body(), "Cloned response must have body");
        assertEquals(payload, Util.toString(cloned.body().asReader(StandardCharsets.UTF_8)));

        assertFalse(appender.list.isEmpty(), "Expected response log");
        String msg = appender.list.get(0).getFormattedMessage();
        assertTrue(msg.contains("Request received - status: 200"));
        assertTrue(msg.contains("elapsedTime: 123ms"));
        assertTrue(msg.contains("content-type"));
        assertTrue(msg.contains(payload));
    }

    @Test
    void logAndRebufferResponse_handlesNullBody() {
        setLogOn(true);

        Response response = Response.builder()
                .status(204)
                .reason("No Content")
                .headers(Collections.emptyMap())
                .request(Request.create(Request.HttpMethod.GET, "http://localhost/empty", Collections.emptyMap(), null, Util.UTF_8, null))
                .build();

        Response cloned = loggerIntegration.logAndRebufferResponse("cfg", FULL, response, 5L);

        assertEquals(204, cloned.status());
        assertNull(cloned.body(), "No body expected in cloned response");
        assertFalse(appender.list.isEmpty(), "Expected log line for response");
        String msg = appender.list.get(0).getFormattedMessage();
        assertTrue(msg.contains("status: 204"));
        assertTrue(msg.contains("elapsedTime: 5ms"));
    }

    @Test
    void log_method_noop() {
        // Ensure the override does not throw
        assertDoesNotThrow(() -> loggerIntegration.log("cfg", "format {} {}", "a", "b"));
    }

}