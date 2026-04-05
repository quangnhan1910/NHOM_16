package com.example.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình chung cho ứng dụng (CORS, interceptors, message converter, ...).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/fonts/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", 
                                      "classpath:/static/images/", "classpath:/static/fonts/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /* Vào gốc → chuyển tới admin (chưa đăng nhập thì Security chặn /admin → /login) */
        registry.addViewController("/").setViewName("redirect:/admin");
    }
}
