package com.huntercodexs.integration.openfeign.starter.annotation;

import com.huntercodexs.core.lib.feignstarter.configuracoes.FeignStarterConfiguracao;
import com.huntercodexs.core.lib.feignstarter.configuracoes.FeignStarterPropriedade;
import com.huntercodexs.core.lib.feignstarter.logger.FeignStarterLogHttp;
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
@EnableFeignClients({"com.huntercodexs"})
@Import({FeignStarterConfiguracao.class, FeignStarterPropriedade.class, FeignStarterLogHttp.class})
public @interface HabilitarFeignStarter {
}
