package com.example.demo.config;

/**
 * 跨域配置
 */
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.jetbrains.annotations.NotNull;---需要引入依赖

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) { //参数需要为非空
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8082") // 生产环境应指定具体域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 与allowedOrigins("*")冲突，需设为false
                .maxAge(3600);
    }
}