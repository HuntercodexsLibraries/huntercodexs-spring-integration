package com.huntercodexs.integration.feignstarter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
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

}
