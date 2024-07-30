package com.zqswjtu.freemall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、开启服务注册发现
 *    1）配置nacos的注册中心地址
 * 2、使用@EnableDiscoveryClient注解
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FreemallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallGatewayApplication.class, args);
    }

}
