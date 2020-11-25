package com.anhndn.assessment.user.config;

import com.anhndn.assessment.user.interceptor.PayloadInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private PayloadInterceptor payloadInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(payloadInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/health")
        .excludePathPatterns("/info");
    }
}