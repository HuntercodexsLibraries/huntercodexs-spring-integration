package com.huntercodexs.integration.openfeign.starter.config;

import br.com.bradesco.core.lib.feignstarter.configuracoes.decoder.FeignStarterErroCustomizadoDecoder;
import feign.Logger;
import feign.Retryer;
import feign.Logger.Level;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignStarterConfiguracao {
    private final FeignStarterPropriedade feignStarterPropriedade;

    public FeignStarterConfiguracao(FeignStarterPropriedade feignStarterPropriedade) {
        this.feignStarterPropriedade = feignStarterPropriedade;
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Level.FULL;
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(this.feignStarterPropriedade.getIntervaloInicial(), this.feignStarterPropriedade.getIntervaloMaximo(), this.feignStarterPropriedade.getMaximoTentativas());
    }

    @Bean
    public ErrorDecoder defaultErrorDecoder() {
        return new FeignStarterErroCustomizadoDecoder();
    }
}
