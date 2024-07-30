package com.zqswjtu.freemall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1、pom文件中引入依赖：RabbitAutoConfiguration就会自动生效
 * 2、给容器中自动配置了RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 *    所有的属性都是spring.rabbitmq开头
 * 3、给配置文件配置spring.rabbitmq信息
 * 4、启动器上配置注解@EnableRabbit
 * 5、监听消息：使用@RabbitListener，必须有@EnableRabbit
 *
 * Seata控制分布式事务
 * 1、每一个微服务必须创建undo_log表
 * 2、安装事务协调器
 * 3、整合
 *    1）解压并启动seata-server
 *       registry.conf：注册中心配置，修改registry type=nacos
 *    2）导入依赖
 *            <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
 *             <version>2021.1</version>
 *         </dependency>
 *    3）SeataRestTemplateAutoConfiguration.class 循环依赖错误，可以发现其源码
 *      自己注入 SeataRestTemplateInterceptor bean，而这个 bean 又是在自己内部创建的。
 *      解决方案：自己重新编写这个配置类，并使用@SpringBootApplication(exclude = SeataRestTemplateAutoConfiguration.class)
 *               排除自动配置
 *               @Configuration(proxyBeanMethods = false)
 *              public class SeataRestTemplateAutoConfiguration {
 *                  public SeataRestTemplateAutoConfiguration() {}
 *
 *                  @Bean
 *                  public static SeataRestTemplateInterceptor seataRestTemplateInterceptor() {
 *                      return new SeataRestTemplateInterceptor();
 *                  }
 *
 *                  @Autowired(required = false)
 *                  private Collection<RestTemplate> restTemplates;
 *                  @Autowired
 *                  private SeataRestTemplateInterceptor seataRestTemplateInterceptor;
 *
 *                  @PostConstruct
 *                  public void init() {
 *                      if (this.restTemplates != null) {
 *                          Iterator var1 = this.restTemplates.iterator();
 *                          while(var1.hasNext()) {
 *                              RestTemplate restTemplate = (RestTemplate)var1.next();
 *                              List<ClientHttpRequestInterceptor> interceptors = new ArrayList(restTemplate.getInterceptors());
 *                              interceptors.add(this.seataRestTemplateInterceptor);
 *                              restTemplate.setInterceptors(interceptors);
 *                          }
 *                      }
 *                  }
 *              }
 *    4）将file.conf 与 registry.conf文件从seata安装文件夹中复制到对应微服务项目的resources文件夹并在file.conf文件中添加如下内容
 *      service {
 *          #vgroup->rgroup
 *          vgroupMapping.${application-name}-seata-service-group = "default"
 *          #only support single node
 *          default.grouplist = "127.0.0.1:8091"
 *          #degrade current not support
 *          enableDegrade = false
 *          #disable
 *          disable = false
 *          #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
 *          max.commit.retry.timeout = "-1"
 *          max.rollback.retry.timeout = "-1"
 *      }
 *      配置文件写入spring.cloud.alibaba.seata.tx-service-group=${application-name}-seata-service-group
 *    5）分布式大事务的入口标注@GlobalTransactional注解，每一个远程的小事务使用@Transactional注解
 */

@EnableRabbit
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.zqswjtu.freemall.order.feign")
@SpringBootApplication
public class FreemallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreemallOrderApplication.class, args);
    }

}
