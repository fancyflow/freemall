package com.zqswjtu.freemall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 远程调用别的微服务模块接口
 * 1）引入open-feign依赖
 * 2）编写一个接口，告诉spring cloud这个接口需要调用远程服务
 *   ①声明接口的每一个方法都是调用哪个远程服务的哪个方法
 * 3）开启远程调用功能
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zqswjtu.freemall.member.feign")
@SpringBootApplication
public class FreemallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallMemberApplication.class, args);
    }

}
