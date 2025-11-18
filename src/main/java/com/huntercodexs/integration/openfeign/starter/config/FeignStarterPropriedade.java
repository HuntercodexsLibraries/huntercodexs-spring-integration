package com.huntercodexs.integration.openfeign.starter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "feign.starter"
)
public class FeignStarterPropriedade {
    @Value("${intervalo-inicial:100}")
    private long intervaloInicial;
    @Value("${intervalo-maximo:1000}")
    private long intervaloMaximo;
    @Value("${maximo-tentativas:3}")
    private int maximoTentativas;

    public long getIntervaloInicial() {
        return this.intervaloInicial;
    }

    public void setIntervaloInicial(long intervaloInicial) {
        this.intervaloInicial = intervaloInicial;
    }

    public long getIntervaloMaximo() {
        return this.intervaloMaximo;
    }

    public void setIntervaloMaximo(long intervaloMaximo) {
        this.intervaloMaximo = intervaloMaximo;
    }

    public int getMaximoTentativas() {
        return this.maximoTentativas;
    }

    public void setMaximoTentativas(int maximoTentativas) {
        this.maximoTentativas = maximoTentativas;
    }
}
