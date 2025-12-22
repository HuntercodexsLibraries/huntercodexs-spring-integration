package com.huntercodexs.integration.rabbitmq.abstractor;

import com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

public class RabbitIntegrationUtilAbstract {

    protected static org.mockito.MockedStatic<RabbitIntegrationUtil> utilRabbitIntegration;

    @BeforeAll
    static void beforeAll() {
        utilRabbitIntegration = Mockito.mockStatic(RabbitIntegrationUtil.class);
    }

    @AfterAll
    static void afterAll() {
        utilRabbitIntegration.close();
    }
}
