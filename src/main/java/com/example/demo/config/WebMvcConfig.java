package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${report.storage.path}")
    private String reportStoragePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把本地 ./resouce/report 映射到 URL /report/**
        String location = "file:" + (reportStoragePath.endsWith("/")
                ? reportStoragePath
                : reportStoragePath + "/");

        registry.addResourceHandler("/report/**")
                .addResourceLocations(location);
    }
}