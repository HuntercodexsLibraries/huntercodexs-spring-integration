package com.huntercodexs.integration.feignstarter.config;

import com.huntercodexs.integration.feignstarter.decoder.FeignStarterErroCustomizadoDecoder;
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
    public Logger.Level feignLoggerLevel2() {
        return Level.FULL;
    }

    @Bean
    public Retryer retryer2() {
        return new Retryer.Default(this.feignStarterPropriedade.getIntervaloInicial(), this.feignStarterPropriedade.getIntervaloMaximo(), this.feignStarterPropriedade.getMaximoTentativas());
    }

    @Bean
    public ErrorDecoder defaultErrorDecoder2() {
        return new FeignStarterErroCustomizadoDecoder();
    }
}
