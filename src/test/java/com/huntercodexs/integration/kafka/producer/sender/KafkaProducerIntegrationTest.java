package com.huntercodexs.integration.kafka.producer.sender;

import com.huntercodexs.integration.kafka.producer.process.KakfaProducerIntegrationProcess;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaProducerIntegrationTest {

    KafkaTemplate<String, String> kafkaTemplate;
    KakfaProducerIntegrationProcess strategy;
    KafkaProducerIntegration subject;

    @BeforeEach
    void init() {
        kafkaTemplate = mock(KafkaTemplate.class);
        strategy = mock(KakfaProducerIntegrationProcess.class);
        // Instancia com \@RequiredArgsConstructor
        subject = new KafkaProducerIntegration(kafkaTemplate, List.of(strategy));

        // Usa reflexão para configurar campos \@Value
        setPrivateField(subject, "kafkaEnabled", true);
        setPrivateField(subject, "topicName", "default-topic");
    }

    @Test
    void desabilitadoDeveLancarIllegalStateException() {
        setPrivateField(subject, "kafkaEnabled", false);
        assertThrows(IllegalStateException.class,
                () -> subject.send("msg", "producer", null));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void produtorNaoEncontradoDeveRetornarSemEnviar() {
        when(strategy.supports("other")).thenReturn(false);
        subject.send("msg", "other", null);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void overrideDeTopicoDeveSerUsado() throws Exception {
        when(strategy.supports("p")).thenReturn(true);
        when(strategy.processMessage("msg")).thenReturn("{\"x\":1}");
        when(strategy.producerRecord("msg")).thenReturn(null);

        SendResult<String, String> sr = mockSendResult(10L, 2);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(sr));

        subject.send("msg", "p", "override-topic");

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertEquals("override-topic", captor.getValue().topic());
    }

    @Test
    void mensagemProcessadaNulaCaiParaStringValue() throws Exception {
        when(strategy.supports("p")).thenReturn(true);
        when(strategy.processMessage("msg")).thenReturn(null); // força caminho nulo
        when(strategy.producerRecord("msg")).thenReturn(null);

        SendResult<String, String> sr = mockSendResult(1L, 0);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(sr));

        subject.send("msg", "p", null);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertEquals("msg", captor.getValue().value()); // caiu no String.valueOf(message)
    }

    @Test
    void headersSaoAdicionadosQuandoFornecidos() throws Exception {
        when(strategy.supports("p")).thenReturn(true);
        when(strategy.processMessage("msg")).thenReturn("ok");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("h1", "v1");
        headers.put("h2", "v2");
        when(strategy.producerRecord("msg")).thenReturn(headers);

        SendResult<String, String> sr = mockSendResult(5L, 1);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(sr));

        subject.send("msg", "p", null);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        RecordHeaders recordHeaders = (RecordHeaders) captor.getValue().headers();
        assertArrayEquals("v1".getBytes(StandardCharsets.UTF_8), recordHeaders.lastHeader("h1").value());
        assertArrayEquals("v2".getBytes(StandardCharsets.UTF_8), recordHeaders.lastHeader("h2").value());
    }

    @Test
    void semHeadersNaoAdicionaNada() throws Exception {
        when(strategy.supports("p")).thenReturn(true);
        when(strategy.processMessage("msg")).thenReturn("ok");
        when(strategy.producerRecord("msg")).thenReturn(null);

        SendResult<String, String> sr = mockSendResult(2L, 0);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(sr));

        subject.send("msg", "p", null);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertNull(captor.getValue().headers().lastHeader("any"));
    }

    @Test
    void interrompidoDeveRestaurarInterrupcaoELogar() throws ExecutionException, InterruptedException {
        when(strategy.supports("p")).thenReturn(true);
        when(strategy.processMessage("msg")).thenReturn("ok");
        when(strategy.producerRecord("msg")).thenReturn(null);

        CompletableFuture<SendResult<String, String>> future = mock(CompletableFuture.class);
        when(future.get()).thenThrow(new InterruptedException("boom"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        // Não deve lançar exceção para fora (tratada internamente)
        subject.send("msg", "p", null);
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void executionExceptionDeveSerTratada() throws ExecutionException, InterruptedException {
        when(strategy.supports("p")).thenReturn(true);
        when(strategy.processMessage("msg")).thenReturn("ok");
        when(strategy.producerRecord("msg")).thenReturn(null);

        CompletableFuture<SendResult<String, String>> future = mock(CompletableFuture.class);
        when(future.get()).thenThrow(new ExecutionException(new RuntimeException("cause")));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        // Não deve lançar exceção para fora (tratada internamente)
        assertDoesNotThrow(() -> subject.send("msg", "p", null));
        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    private static SendResult<String, String> mockSendResult(long offset, int partition) {
        SendResult<String, String> sr = mock(SendResult.class, RETURNS_DEEP_STUBS);
        when(sr.getRecordMetadata().offset()).thenReturn(offset);
        when(sr.getRecordMetadata().partition()).thenReturn(partition);
        return sr;
    }

    private static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = KafkaProducerIntegration.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}