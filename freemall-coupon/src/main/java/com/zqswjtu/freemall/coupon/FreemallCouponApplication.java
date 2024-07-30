package com.zqswjtu.freemall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、想要远程调用别的服务
 *    1）引入open-feign
 *    2）编写一个接口，告诉SpringCloud这个接口需要调用远程服务
 *    3）声明接口的每一个方法都是调用哪个远程服务的哪个请求
 * 2、如何使用Nacos作为配置中心统一管理配置
 *    1）引入依赖
 *       <!--配置中心来做配置管理-->
 *       <dependency>
 *           <groupId>com.alibaba.cloud</groupId>
 *           <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
 *       </dependency>
 *    2）创建bootstrap.properties文件，填写如下内容：
 *       spring.application.name=应用名称
 *       spring.cloud.nacos.config.server-addr=127.0.0.1:8848
 *    3）上述环节完成后，启动应用报错：No spring.config.import property has been defined
 *       原因：spring cloud 2020禁用了bootstrap，导致在读取文件的时候读取不到而报错
 *       解决方案：导入如下依赖：
 *           <dependency>
 *               <groupId>org.springframework.cloud</groupId>
 *               <artifactId>spring-cloud-starter-bootstrap</artifactId>
 *               <version>3.0.3</version>
 *           </dependency>
 *    4）需要给配置中心添加一个默认的数据集(Data Id) freemall-coupon.properties。默认规则：应用名.properties
 *    5）给应用名.properties添加任何配置
 *    6）动态获取配置
 *       在对应Controller上添加@RefreshScope注解动态获取并刷新配置
 *       在对应的字段上使用@Value("${配置项名称}")获取配置内容
 *       如果配置中心和当前应用的配置文件都配置了相同的项，则优先使用配置中心的配置
 *    7）细节
 *       ①命名空间：配置隔离
 *         默认是public(保留空间)：默认新增的所有配置都在public空间
 *       ②开发、测试、生产环境可以使用命名空间来进行环境隔离
 *         注：在bootstrap.properties中配置使用哪个命名空间下的配置
 *         spring.cloud.nacos.config.namespace=c19285c4-16bb-4297-b284-9998aee1a31f
 *       ③每一个微服务之间相互隔离配置，每个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
 *       ④配置集：所有配置的集合
 *       ⑤配置集Id(Data Id)：类似文件名
 *       ⑥配置分组：默认所有的配置集都属于DEFAULT_GROUP，可以自己新建分组
 *         注：在bootstrap.properties中配置使用对应命名空间下哪个分组的配置
 *         spring.cloud.nacos.config.group=TEST_GROUP
 *      项目中的使用每个微服务都创建自己的命名空间，使用配置分组区分环境：dev、test、prod
 * 3、同时加载多个配置集
 *    1）微服务任何配置信息、任何配置文件都可以放在配置中心
 *    2）只需要在bootstrap.properties中说明加载配置中心哪些配置文件即可
 *    3）@Value、@ConfigurationProperties等SpringBoot任何从配置文件中获取值的方法都能从配置中心获取对应值
 */
@EnableDiscoveryClient
@SpringBootApplication
public class FreemallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallCouponApplication.class, args);
    }

}
