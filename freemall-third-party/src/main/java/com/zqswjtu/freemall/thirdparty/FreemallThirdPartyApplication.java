package com.zqswjtu.freemall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FreemallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallThirdPartyApplication.class, args);
    }

}
