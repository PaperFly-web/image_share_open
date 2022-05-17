package com.paperfly.imageShare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class CorsConfig  {
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConf = new CorsConfiguration();
        corsConf.addAllowedOrigin("*");//允许所有跨域请求
        corsConf.addAllowedHeader("*");//放行全部原始头信息
        corsConf.addAllowedMethod("*");//允许所有类型的请求方式
        source.registerCorsConfiguration("/**", corsConf);
        return new CorsFilter(source);
    }

}