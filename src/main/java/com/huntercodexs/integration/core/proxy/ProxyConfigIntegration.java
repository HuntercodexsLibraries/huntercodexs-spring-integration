package com.huntercodexs.integration.core.proxy;

import jakarta.annotation.PostConstruct;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.huntercodexs.integration.core.constants.CoreIntegrationConstants.CORE_LOGGING_APP_CONFIG;
import static com.huntercodexs.integration.core.constants.CoreIntegrationConstants.CORE_PROXY_APP_CONFIG;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Configuration
@Profile({"local", "dev", "develop", "development", "default", "stage", "prod", "production"})
public class ProxyConfigIntegration {

    @Generated
    private static final Logger log = LoggerFactory.getLogger(ProxyConfigIntegration.class);

    @Value("${"+ CORE_LOGGING_APP_CONFIG +".enabled:false}")
    private boolean enableLogging;

    @Value("${"+ CORE_PROXY_APP_CONFIG +".enable:false}")
    private boolean enableProxy;

    @Value("${"+ CORE_PROXY_APP_CONFIG +".host:null}")
    private String proxyHost;

    @Value("${"+ CORE_PROXY_APP_CONFIG +".port:null}")
    private String proxyPort;

    @PostConstruct
    public void properties() {
        if (this.enableProxy) {
            System.setProperty("https.proxyHost", this.proxyHost);
            System.setProperty("https.proxyPort", this.proxyPort);
            System.setProperty("https.proxySet", "true");
            System.setProperty("http.proxyHost", this.proxyHost);
            System.setProperty("http.proxyPort", this.proxyPort);
            System.setProperty("http.proxySet", "true");
            if (enableLogging) {
                log.info("Feign Proxy enabled on {}:{}", this.proxyHost, this.proxyPort);
            }
            if (isBlank(proxyHost) || isBlank(proxyPort)) {
                log.error("There is some error in your Feign Proxy configuration, check host [{}] and port [{}]", this.proxyHost, this.proxyPort);
            }
        } else {
            if (enableLogging) {
                log.info("Feign Proxy is disabled");
            }
        }
    }

}
