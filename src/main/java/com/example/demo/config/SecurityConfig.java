package com.example.demo.config;

import com.example.demo.utils.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

//  开发环境：允许所有请求，无需认证
     @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         http
                 .csrf(csrf -> csrf.disable())
                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                 .sessionManagement(session -> session
                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                 )
                 // 关闭 X-Frame-Options，允许被 iframe 加载（开发环境）
                 .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                 // 开发环境：允许所有请求，无需认证
                 .authorizeHttpRequests(auth -> auth
                         .requestMatchers("/**").permitAll()
                 );

         // 注释掉JWT过滤器
         // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

         return http.build();
     }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http
    //         .csrf(csrf -> csrf.disable())  // 禁用CSRF，因为使用JWT
    //         .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用CORS配置
    //         .sessionManagement(session -> session
    //             .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 使用无状态会话
    //         )
    //         .authorizeHttpRequests(auth -> auth
    //             .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()  // 认证相关端点放行
    //             .requestMatchers("/api/auth/logout").authenticated()  // 登出需要认证
    //             .requestMatchers(
    //                 "/swagger-ui/**", 
    //                 "/v3/api-docs/**", 
    //                 "/swagger-resources/**"
    //             ).permitAll()  // Swagger放行
    //             .anyRequest().authenticated()  // 其他请求需要认证
    //         )
    //         .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // 添加JWT过滤器
        
    //     return http.build();
    // }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:8080",
            "http://localhost:8082",
            "https://your-production-domain.com"
        )); // 配置允许的源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1小时
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // 设置强度
    }
}












// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Lazy;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// import com.example.demo.utils.JwtAuthenticationFilter;

// @Configuration// 标记为Spring配置类
// @EnableWebSecurity//启用Spring Security
// public class SecurityConfig {

//     private final JwtAuthenticationFilter jwtAuthenticationFilter;

//     // 构造函数注入Jwt过滤器（使用@Lazy解决循环依赖）
//     public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
//         this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//     }
//     // 配置安全过滤链，配置哪些请求需要认证，哪些可以公开访问
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable()) // 禁用CSRF，因为使用JWT
//             .cors(cors -> cors.disable()) // 禁用CORS，或根据需要配置(由前端处理或Nginx配置)

//             .sessionManagement(session -> session
//                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 使用无状态会话

//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers("/api/auth/**").permitAll() // 认证相关端点放行
//                 .requestMatchers(
//                     "/swagger-ui/**", 
//                     "/v3/api-docs/**",
//                     "/swagger-resources/**"
//                     ).permitAll() // Swagger放行
//                 .anyRequest().authenticated() // 其他请求需要认证
//             )
//             .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 添加JWT过滤器

//         return http.build();
//     }
//     //认证管理器，用于处理用户认证,暴露AuthenticationManager到Spring容器
//     @Bean
//     public AuthenticationManager authenticationManager(
//         AuthenticationConfiguration config) throws Exception {
//         return config.getAuthenticationManager();
//     }
//     //密码编码器，用于密码加密和验证
//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }
// }