package com.zqswjtu.freemall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FreemallFeignConfig {

//    @Bean("requestInterceptor")
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            HttpServletRequest request = attributes.getRequest(); // 原来的老请求
//            String cookie = request.getHeader("Cookie");
//            // 给新请求同步老请求的cookie
//            requestTemplate.header("Cookie", cookie);
//        };
//    }
}
