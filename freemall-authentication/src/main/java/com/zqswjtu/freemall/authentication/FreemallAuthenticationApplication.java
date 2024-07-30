package com.zqswjtu.freemall.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession
public class FreemallAuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallAuthenticationApplication.class, args);
    }

}
