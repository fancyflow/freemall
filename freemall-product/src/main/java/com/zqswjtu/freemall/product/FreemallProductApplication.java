package com.zqswjtu.freemall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合mybatis-plus
 *    1）导入依赖
 *    <dependency>
 *        <groupId>com.baomidou</groupId>
 *        <artifactId>mybatis-plus-boot-starter</artifactId>
 *        <version>3.2.0</version>
 *    </dependency>
 *    2）配置
 *        1、配置数据源
 *            ①导入数据库驱动
 *            ②在application.yml中配置数据源相关信息
 *        2、配置mybatis-plus
 *            ①使用@MapperrScan
 *            ②告诉mybatis-plus框架sql映射文件位置
 * 2、mybatis-plus使用逻辑删除的方法
 *    1）配置全局的逻辑删除规则
 *    2）配置逻辑删除的组件Bean(3.1.1以后不用配置)
 *    3）需要在实体类对应字段上加上逻辑删除注解@TableLogic
 * 3、JSR303
 *    1）给Bean添加校验注解：javax.validation.constraints，并定义自己的message提示
 *    2）在controller层需要进行校验的方法参数前加上一个@Valid注解开启校验功能
 *    3）给校验的参数后紧跟一个BindingResult对象就可以获得校验的结果
 *    4）分组校验
 *       ①给检验注解的groups属性赋值，标注什么情况下需要进行校验
 *         @NotNull(message = "修改必须指定id", groups = {UpdateGroup.class})
 * 	       @Null(message = "新增不能指定id", groups = {AddGroup.class})
 *         @TableId
 *         private Long brandId;
 *       ②在controller层需要进行分组校验的方法参数前加上一个@Validated注解，并给其value属性赋值标注什么情况下需要进行校验
 *         public R save(@Validated(value = {AddGroup.class}) @RequestBody BrandEntity brand)
 *       ③如果使用的是分组校验，但是没有给校验注解的groups属性赋值则默认是校验不生效
 *    5）自定义校验
 *       ①编写一个自定义校验注解
 *       ②编写一个自定义的校验器
 *       ③关联自定义的校验器和自定义的校验注解
 * 4、统一的异常处理：@RestControllerAdvice
 *    1）编写统一异常处理类，类上面使用@RestControllerAdvice注解
 *    2）类方法上使用@ExceptionHandler注解处理对应的异常状况(注解可以使用value说明处理哪种异常)
 * 5、整合Redis
 *    1）引入redis依赖
 *    2）配置redis的host信息
 *    3）使用Spring Boot自动配置好的StringRedisTemplate来操作Redis
 */
@EnableCaching
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.zqswjtu.freemall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.zqswjtu.freemall.product.dao")
@SpringBootApplication
public class FreemallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallProductApplication.class, args);
    }

}
