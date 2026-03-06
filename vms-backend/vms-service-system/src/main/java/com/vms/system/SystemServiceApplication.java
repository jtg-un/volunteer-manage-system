package com.vms.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 系统服务启动类（端口8087）
 * 负责：字典管理、行政区划
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.vms")
@MapperScan("com.vms.repository.mapper")
public class SystemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
    }
}