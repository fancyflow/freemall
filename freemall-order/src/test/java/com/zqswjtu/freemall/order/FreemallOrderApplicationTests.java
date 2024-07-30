package com.zqswjtu.freemall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FreemallOrderApplicationTests {
    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 1、创建Exchange、Queue、Binding
     * 2、收发消息
     *    如果发送的消息是一个对象，则该类必须实现序列化(实现Serializable接口)
     */
    @Test
    void contextLoads() {
        System.out.println(amqpAdmin);
        System.out.println(rabbitTemplate);
    }

}
