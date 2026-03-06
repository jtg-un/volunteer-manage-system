package com.vms.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类（端口8081）
 * 负责：登录、注册、组织审核
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.vms")
@MapperScan("com.vms.repository.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}