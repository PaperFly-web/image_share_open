package com.paperfly.imageShare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("paperfly")
                .enable(true)//设置是否能在浏览器中访问Swagger
                .select()
                //RequestHandlerSelectors，配置要扫描接口的方式
                //basePackage:指定要扫描的包
                //any():扫描全部
                //none():不扫描
                //withClassAnnotation:扫描类上的注解，参数是一个注解的反射对象
                //withMethodAnnotation:扫描方法上的注解
                .apis(RequestHandlerSelectors.basePackage("com.paperfly.imageShare.controller"))
                //过滤的作用，代表只扫描那种URL
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        //作者信息
        Contact contact=new Contact("paperfly","http://localhost:8002","1430978392@qq.com");
        return new ApiInfo(
                "PaperFly使用的Swagger",
                "加油努力",
                "1.0.1",
                "http://localhost:8001",
                contact,//license
                "Apache",
                "http://localhost:8080",
                new ArrayList<>()
        );
    }

}