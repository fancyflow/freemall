package com.zqswjtu.freemall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession
public class FreemallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallSearchApplication.class, args);
    }

}
