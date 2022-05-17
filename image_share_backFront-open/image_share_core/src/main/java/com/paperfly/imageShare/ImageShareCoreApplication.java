package com.paperfly.imageShare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.paperfly.imageShare.dao")
@EnableAspectJAutoProxy(proxyTargetClass=true,exposeProxy = true)
public class ImageShareCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageShareCoreApplication.class, args);
    }
}
