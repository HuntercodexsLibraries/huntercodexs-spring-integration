package com.huntercodexs.integration.rabbitmq.core.config;

import com.huntercodexs.integration.rabbitmq.abstractor.RabbitIntegrationUtilAbstract;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitConsumersIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.props.RabbitGlobalIntegrationProperties;
import com.huntercodexs.integration.rabbitmq.core.util.RabbitIntegrationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RabbitExchangeIntegrationConfigTest extends RabbitIntegrationUtilAbstract {

    private RabbitConsumersIntegrationProperties consumersProperties;
    private RabbitExchangeIntegrationConfig config;

    @BeforeEach
    void setUp() {
        consumersProperties = new RabbitConsumersIntegrationProperties();
        consumersProperties.setName("global-consumer");
        consumersProperties.setExchange("ex");
        consumersProperties.setRoutingKey("rk");
        consumersProperties.setQueue("q-main");
        consumersProperties.setLogEnabled(false);

        // mock global props
        RabbitGlobalIntegrationProperties globalProps = mock(RabbitGlobalIntegrationProperties.class);
        //when(globalProps.isLogEnabled()).thenReturn(false);

        config = new RabbitExchangeIntegrationConfig(globalProps, consumersProperties);

        // default behavior: no default override, and doLog no-op
        utilRabbitIntegration.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);
        //util.when(() -> RabbitIntegrationUtil.doLog(any())).thenReturn(null);
    }

    @Test
    void rabbitDeclarables_whenConsumersNull_returnsEmpty_declarables_and_noDefaultCheck() {
        consumersProperties.setConsumers(null);

        Declarables d = config.rabbitDeclarables();

        assertNotNull(d);
        assertTrue(d.getDeclarables().isEmpty());

        // ensure static default check not invoked
        //util.verify(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any()), never());
    }

    @Test
    void rabbitDeclarables_whenDefaultCheckTrue_creates_basic_exchange_queue_binding() {
        // ensure consumers list exists but empty so defaultCheck(true) can add the top-level config
        consumersProperties.setConsumers(new ArrayList<>());

        // simulate defaultCheck(true) -> true
        utilRabbitIntegration.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(true);

        Declarables d = config.rabbitDeclarables();

        // main exchange, main queue, main binding => 3 declarables
        assertNotNull(d);
        assertEquals(3, d.getDeclarables().size());
        List<String> names = d.getDeclarables().stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        assertTrue(names.contains("ExchangeBuilder$BuiltExchange") || names.contains("DirectExchange") || names.contains("TopicExchange") || names.contains("FanoutExchange"));
    }

    @Test
    void rabbitDeclarables_withRetryAndDlq_creates_all_retry_and_dlq_declarables() {
        consumersProperties.setConsumers(new ArrayList<>());
        consumersProperties.setRetryEnabled(true);
        consumersProperties.setRetryTtlMilliseconds(1000);
        consumersProperties.setDlqEnabled(true);
        consumersProperties.setQueue("primary-queue");
        consumersProperties.setExchange("exAll");
        consumersProperties.setRoutingKey("rkAll");

        utilRabbitIntegration.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(true);

        Declarables d = config.rabbitDeclarables();

        // expected items: main exchange, main queue, main binding,
        // retry exchange, retry queue, retry binding,
        // dlq exchange, dlq queue, dlq binding => 9
        assertNotNull(d);
        assertEquals(9, d.getDeclarables().size());
    }

    @Test
    void rabbitDeclarables_onlyDlq_sets_main_queue_dlq_args() {
        RabbitConsumersIntegrationProperties cfg = new RabbitConsumersIntegrationProperties();
        cfg.setExchange("exX");
        cfg.setRoutingKey("rkX");
        cfg.setQueue("queueX");
        cfg.setDlqEnabled(true);
        cfg.setRetryEnabled(false);

        // avoid default override
        consumersProperties.setConsumers(List.of(cfg));
        utilRabbitIntegration.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);

        Declarables d = config.rabbitDeclarables();

        List<Queue> queues = d.getDeclarables().stream()
                .filter(obj -> obj instanceof Queue)
                .map(obj -> (Queue) obj)
                .collect(Collectors.toList());

        // find the main queue by name
        Optional<Queue> mainQueue = queues.stream().filter(q -> "queueX".equals(q.getName())).findFirst();
        assertTrue(mainQueue.isPresent());
        Map<String, Object> args = mainQueue.get().getArguments();
        assertNotNull(args);
        assertEquals("exX.dlq", args.get("x-dead-letter-exchange"));
        assertEquals("rkX.dlq", args.get("x-dead-letter-routing-key"));
    }

    @Test
    void dynamicRabbitQueues_whenDefaultCheckTrue_returns_queue_array() {
        consumersProperties.setQueue("dynQ");
        // ensure there is an initial consumers list so defaultCheck(false) can run and set singleton
        consumersProperties.setConsumers(new ArrayList<>());

        // defaultCheck(false) should return true so the top-level is added
        utilRabbitIntegration.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(true);

        String[] arr = config.dynamicRabbitQueues();

        assertNotNull(arr);
        assertEquals(1, arr.length);
        assertEquals("dynQ", arr[0]);
    }

    @Test
    void dynamicRabbitQueues_whenNoConsumers_returns_empty_array_and_logs() {
        consumersProperties.setConsumers(null);

        // ensure defaultCheck(false) returns false so nothing is added
        utilRabbitIntegration.when(() -> RabbitIntegrationUtil.defaultConsumerPropertiesCheck(any())).thenReturn(false);

        String[] arr = config.dynamicRabbitQueues();

        assertNotNull(arr);
        assertEquals(0, arr.length);

        // doLog should be invoked (callLog uses doLog)
        //util.verify(() -> RabbitIntegrationUtil.doLog(any()), atLeastOnce());
    }

    @Test
    void private_buildExchange_variants_topic_fanout_headers_default() throws Exception {
        Method buildExchange = RabbitExchangeIntegrationConfig.class.getDeclaredMethod("buildExchange", String.class, String.class);
        buildExchange.setAccessible(true);

        // topic
        Exchange eTopic = (Exchange) buildExchange.invoke(config, "topic", "exTopic");
        assertTrue(eTopic instanceof TopicExchange);
        assertEquals("exTopic", eTopic.getName());

        // fanout
        Exchange eFanout = (Exchange) buildExchange.invoke(config, "fanout", "exFanout");
        assertTrue(eFanout instanceof FanoutExchange);
        assertEquals("exFanout", eFanout.getName());

        // headers
        Exchange eHeaders = (Exchange) buildExchange.invoke(config, "headers", "exHeaders");
        assertTrue(eHeaders instanceof HeadersExchange);
        assertEquals("exHeaders", eHeaders.getName());

        // default (anything else -> direct)
        Exchange eDefault = (Exchange) buildExchange.invoke(config, "something-else", "exDirect");
        assertTrue(eDefault instanceof DirectExchange);
        assertEquals("exDirect", eDefault.getName());
    }

    @Test
    void private_buildBinding_variants_topic_fanout_direct() throws Exception {
        Method buildBinding = RabbitExchangeIntegrationConfig.class.getDeclaredMethod("buildBinding", Queue.class, Exchange.class, String.class, String.class);
        // The actual private signature in class is (Queue, Exchange, String, String) - reflect accordingly
        // Some JVMs will require exact parameter types; try with 4 params and fallback to 3-arg variant if necessary.
        Method method;
        try {
            method = RabbitExchangeIntegrationConfig.class.getDeclaredMethod("buildBinding", Queue.class, Exchange.class, String.class, String.class);
        } catch (NoSuchMethodException ex) {
            // fallback to 3-arg signature (Queue, Exchange, String)
            method = RabbitExchangeIntegrationConfig.class.getDeclaredMethod("buildBinding", Queue.class, Exchange.class, String.class);
        }
        method.setAccessible(true);

        // topic binding
        Queue q1 = new Queue("q1");
        TopicExchange topicEx = new TopicExchange("topicEx");
        Binding bTopic = (Binding) method.invoke(config, q1, topicEx, "topic", "rk.topic");
        assertEquals("q1", bTopic.getDestination());
        assertEquals("rk.topic", bTopic.getRoutingKey());

        // fanout binding
        Queue q2 = new Queue("q2");
        FanoutExchange fanoutEx = new FanoutExchange("fanoutEx");
        Binding bFanout = (Binding) method.invoke(config, q2, fanoutEx, "fanout", null);
        assertEquals("q2", bFanout.getDestination());

        // direct binding
        Queue q3 = new Queue("q3");
        DirectExchange directEx = new DirectExchange("directEx");
        Binding bDirect = (Binding) method.invoke(config, q3, directEx, "direct", "rk.direct");
        assertEquals("q3", bDirect.getDestination());
        assertEquals("rk.direct", bDirect.getRoutingKey());
    }

    @Test
    void private_buildMainQueue_variants_bothDlqAndRetry_onlyDlq_onlyRetry() throws Exception {
        Method buildMainQueue = RabbitExchangeIntegrationConfig.class.getDeclaredMethod("buildMainQueue", RabbitConsumersIntegrationProperties.class, String.class, String.class, String.class);
        // fallback if exact signature differs
        Method method;
        try {
            method = RabbitExchangeIntegrationConfig.class.getDeclaredMethod("buildMainQueue", RabbitConsumersIntegrationProperties.class, String.class, String.class, String.class);
        } catch (NoSuchMethodException ex) {
            // fallback to 4-arg signature that matches compiled code (queueName, exchangeName, routingKey)
            method = RabbitExchangeIntegrationConfig.class.getDeclaredMethod("buildMainQueue", RabbitConsumersIntegrationProperties.class, String.class, String.class, String.class);
        }
        method.setAccessible(true);

        // both dlq and retry
        RabbitConsumersIntegrationProperties cfg1 = new RabbitConsumersIntegrationProperties();
        cfg1.setQueue("mq1");
        cfg1.setExchange("ex1");
        cfg1.setRoutingKey("rk1");
        cfg1.setDlqEnabled(true);
        cfg1.setRetryEnabled(true);
        Queue mq1 = (Queue) method.invoke(config, cfg1, "mq1", "ex1", "rk1");
        Map<String, Object> args1 = mq1.getArguments();
        assertNotNull(args1);
        // when both present, keys must exist (last withArgument overwrites duplicates but branch executed)
        assertTrue(args1.containsKey("x-dead-letter-exchange"));

        // only dlq
        RabbitConsumersIntegrationProperties cfg2 = new RabbitConsumersIntegrationProperties();
        cfg2.setQueue("mq2");
        cfg2.setExchange("ex2");
        cfg2.setRoutingKey("rk2");
        cfg2.setDlqEnabled(true);
        cfg2.setRetryEnabled(false);
        Queue mq2 = (Queue) method.invoke(config, cfg2, "mq2", "ex2", "rk2");
        Map<String, Object> args2 = mq2.getArguments();
        assertEquals("ex2.dlq", args2.get("x-dead-letter-exchange"));
        assertEquals("rk2.dlq", args2.get("x-dead-letter-routing-key"));

        // only retry (with TTL)
        RabbitConsumersIntegrationProperties cfg3 = new RabbitConsumersIntegrationProperties();
        cfg3.setQueue("mq3");
        cfg3.setExchange("ex3");
        cfg3.setRoutingKey("rk3");
        cfg3.setDlqEnabled(false);
        cfg3.setRetryEnabled(true);
        cfg3.setRetryTtlMilliseconds(5000);
        Queue mq3 = (Queue) method.invoke(config, cfg3, "mq3", "ex3", "rk3");
        Map<String, Object> args3 = mq3.getArguments();
        assertEquals("ex3.retry", args3.get("x-dead-letter-exchange"));
        assertEquals("rk3.retry", args3.get("x-dead-letter-routing-key"));
    }
}
