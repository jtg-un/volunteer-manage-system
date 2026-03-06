package com.vms.activity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 活动服务启动类（端口8083）
 * 负责：活动发布、岗位管理、活动检索
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.vms")
@MapperScan("com.vms.repository.mapper")
public class ActivityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityServiceApplication.class, args);
    }
}