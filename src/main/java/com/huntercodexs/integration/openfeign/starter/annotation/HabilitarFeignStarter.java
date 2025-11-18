package com.huntercodexs.integration.openfeign.starter.annotation;

import br.com.bradesco.core.lib.feignstarter.configuracoes.FeignStarterConfiguracao;
import br.com.bradesco.core.lib.feignstarter.configuracoes.FeignStarterPropriedade;
import br.com.bradesco.core.lib.feignstarter.logger.FeignStarterLogHttp;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@EnableFeignClients({"br.com.bradesco"})
@Import({FeignStarterConfiguracao.class, FeignStarterPropriedade.class, FeignStarterLogHttp.class})
public @interface HabilitarFeignStarter {
}
