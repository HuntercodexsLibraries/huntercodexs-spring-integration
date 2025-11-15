package com.huntercodexs.openfeign.proxy;

import jakarta.annotation.PostConstruct;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "default"})
public class FeignProxyConfig {

    @Generated
    private static final Logger log = LoggerFactory.getLogger(FeignProxyConfig.class);

    @Value("${huntercodexs-spring-open-feign.client.config.logging:false}")
    private boolean enableLogging;

    @Value("${huntercodexs-spring-open-feign.client.config.proxy.enable:false}")
    private boolean enableProxy;

    @Value("${huntercodexs-spring-open-feign.client.config.proxy.host:null}")
    private String proxyHost;

    @Value("${huntercodexs-spring-open-feign.client.config.proxy.port:null}")
    private String proxyPort;

    @PostConstruct
    public void properties() {
        if (this.enableProxy) {
            System.setProperty("https.proxyHost", this.proxyHost);
            System.setProperty("https.proxyPort", this.proxyPort);
            System.setProperty("https.proxySet", "true");
            System.setProperty("http.proxyHost", this.proxyHost);;
            System.setProperty("http.proxyPort", this.proxyPort);
            System.setProperty("http.proxySet", "true");
            if (enableLogging) {
                log.info("Feign Proxy enabled on {}:{}", this.proxyHost, this.proxyPort);
            }
        } else {
            if (enableLogging) {
                log.info("Feign Proxy is disabled");
            }
        }
    }

}
