package com.huntercodexs.integration.rabbitmq.core.util;

import com.huntercodexs.integration.rabbitmq.core.dto.RabbitDefaultIntegrationDto;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("java:S5976")
class RabbitIntegrationUtilTest {

    static class MockDto extends RabbitDefaultIntegrationDto {
        private String name;
        private String exchange;
        private String routingKey;
        @Setter
        private boolean logEnabled = true;
        private String logText;
        private Object[] logArgs;

        public MockDto(String name, String exchange, String routingKey) {
            this.name = name;
            this.exchange = exchange;
            this.routingKey = routingKey;
        }

        @Override public String getName() { return name; }
        @Override public String getExchange() { return exchange; }
        @Override public String getRoutingKey() { return routingKey; }
        @Override public boolean isLogEnabled() { return logEnabled; }
        @Override public void setLogText(String logText) { this.logText = logText; }
        @Override public String getLogText() { return logText; }
        @Override public Object[] getLogArgs() { return logArgs; }
    }

    @Test
    void allPropertiesPresent_returnsTrue_setsDefaultMsg() {
        MockDto dto = new MockDto("n", "e", "r");
        dto.setLogEnabled(true);
        boolean result = RabbitIntegrationUtil.defaultConsumerPropertiesCheck(dto);
        assertTrue(result);
        assertEquals("==> Single RabbitMQ consumer properties detected (name, exchange, routingKey), overriding 'consumers' list with a single entry. Is it intended?", dto.getLogText());
    }

    @Test
    void nameBlank_returnsFalse_setsListMsg() {
        MockDto dto = new MockDto(" ", "e", "r");
        dto.setLogEnabled(true);
        boolean result = RabbitIntegrationUtil.defaultConsumerPropertiesCheck(dto);
        assertFalse(result);
        assertEquals("==> List RabbitMQ consumers and producers detected", dto.getLogText());
    }

    @Test
    void exchangeBlank_returnsFalse_setsListMsg() {
        MockDto dto = new MockDto("n", " ", "r");
        dto.setLogEnabled(true);
        boolean result = RabbitIntegrationUtil.defaultConsumerPropertiesCheck(dto);
        assertFalse(result);
        assertEquals("==> List RabbitMQ consumers and producers detected", dto.getLogText());
    }

    @Test
    void routingKeyBlank_returnsFalse_setsListMsg() {
        MockDto dto = new MockDto("n", "e", " ");
        dto.setLogEnabled(true);
        boolean result = RabbitIntegrationUtil.defaultConsumerPropertiesCheck(dto);
        assertFalse(result);
        assertEquals("==> List RabbitMQ consumers and producers detected", dto.getLogText());
    }

    @Test
    void allBlank_returnsFalse_setsListMsg() {
        MockDto dto = new MockDto(" ", " ", " ");
        dto.setLogEnabled(true);
        boolean result = RabbitIntegrationUtil.defaultConsumerPropertiesCheck(dto);
        assertFalse(result);
        assertEquals("==> List RabbitMQ consumers and producers detected", dto.getLogText());
    }

    @Test
    void logDisabled_noException() {
        MockDto dto = new MockDto("n", "e", "r");
        dto.setLogEnabled(false);
        assertDoesNotThrow(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(dto));
    }

    @Test
    void removesNewlinesAndBackslashes() {
        String input = "\n\\n\r\\\"test\\\"\n";
        String expected = "test";
        assertEquals(expected, RabbitIntegrationUtil.stripPayload(input));
    }

    @Test
    void removesLeadingAndTrailingQuotes() {
        String input = "\"payload\"";
        String expected = "payload";
        assertEquals(expected, RabbitIntegrationUtil.stripPayload(input));
    }

    @Test
    void removesOnlyLeadingQuote() {
        String input = "\"payload";
        String expected = "payload";
        assertEquals(expected, RabbitIntegrationUtil.stripPayload(input));
    }

    @Test
    void removesOnlyTrailingQuote() {
        String input = "payload\"";
        String expected = "payload";
        assertEquals(expected, RabbitIntegrationUtil.stripPayload(input));
    }

    @Test
    void removesAllBackslashes() {
        String input = "pay\\load";
        String expected = "payload";
        assertEquals(expected, RabbitIntegrationUtil.stripPayload(input));
    }

    @Test
    void emptyStringReturnsEmpty() {
        assertEquals("", RabbitIntegrationUtil.stripPayload(""));
    }

    @Test
    void stringWithOnlySpecialCharsReturnsEmpty() {
        String input = "\n\\n\r\\\"\"";
        assertEquals("", RabbitIntegrationUtil.stripPayload(input));
    }

    @Test
    void stringWithoutSpecialCharsReturnsSame() {
        String input = "payload";
        assertEquals("payload", RabbitIntegrationUtil.stripPayload(input));
    }
}