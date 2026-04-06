package com.duoc.veterinaria.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpsConfig {

    @Bean
    public ServletWebServerFactory servletContainer(
            @Value("${server.ssl.enabled:false}") boolean sslEnabled,
            @Value("${app.http.port:8080}") int httpPort,
            @Value("${server.port:8384}") int httpsPort) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

        if (sslEnabled && httpPort > 0) {
            tomcat.addAdditionalTomcatConnectors(createHttpRedirectConnector(httpPort, httpsPort));
        }

        return tomcat;
    }

    private Connector createHttpRedirectConnector(int httpPort, int httpsPort) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        return connector;
    }
}
