package com.huntercodexs.integration.feignstarter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.huntercodexs.integration.feignstarter.config.FeignStarterConfiguracao;
import com.huntercodexs.integration.feignstarter.config.FeignStarterPropriedade;
import com.huntercodexs.integration.feignstarter.logger.FeignStarterLogHttp;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@EnableFeignClients({"com.huntercodexs"})
@Import({FeignStarterConfiguracao.class, FeignStarterPropriedade.class, FeignStarterLogHttp.class})
public @interface EnabledFeignIntegration {
}
