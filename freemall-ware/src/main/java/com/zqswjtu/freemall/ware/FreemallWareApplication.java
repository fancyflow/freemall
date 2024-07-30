package com.zqswjtu.freemall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableRabbit
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan(basePackages = "com.zqswjtu.freemall.ware.dao")
@EnableFeignClients(basePackages = "com.zqswjtu.freemall.ware.feign")
@SpringBootApplication
public class FreemallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallWareApplication.class, args);
    }

}
