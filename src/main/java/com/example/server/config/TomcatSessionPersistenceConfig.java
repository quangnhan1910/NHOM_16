package com.example.server.config;

import org.apache.catalina.session.StandardManager;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tránh Tomcat đọc/ghi SESSIONS.ser khi khởi động lại (đặc biệt với spring-boot-devtools):
 * file session cũ + classloader mới dễ gây ClassCastException khi deserialize
 * {@code SPRING_SECURITY_CONTEXT}.
 */
@Configuration
public class TomcatSessionPersistenceConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> disableTomcatSessionSerFile() {
        return factory -> factory.addContextCustomizers(context -> {
            if (context.getManager() instanceof StandardManager sm) {
                sm.setPathname(null);
            }
        });
    }
}
