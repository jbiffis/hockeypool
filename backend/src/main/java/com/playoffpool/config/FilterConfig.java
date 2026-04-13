package com.playoffpool.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilter() {
        FilterRegistrationBean<AdminAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AdminAuthFilter());
        bean.addUrlPatterns("/api/admin/*");
        bean.setOrder(1);
        return bean;
    }
}
