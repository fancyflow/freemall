package com.zqswjtu.freemall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 创建队列，交换机，延时队列，绑定关系 的configuration
 * 1.Broker中的Queue、Exchange、Binding不存在的情况下，会自动创建（在RabbitMQ），不会重复创建覆盖
 * 2.懒加载，只有第一次使用的时候才会创建（例如监听队列）
 */
@Configuration
public class FreemallRabbitMQConfig {
    // 使用注解注入需要使用到的 Binding Queue Exchange
    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        /**
         * Queue(String name,  队列名字
         *       boolean durable,  是否持久化
         *       boolean exclusive,  是否排他
         *       boolean autoDelete, 是否自动删除
         *       Map<String, Object> arguments) 属性【TTL、死信路由、死信路由键】
         */
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");// 死信路由
        arguments.put("x-dead-letter-routing-key", "order.release.order");// 死信路由键
        arguments.put("x-message-ttl", 60000); // 消息过期时间 1分钟
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Queue orderSeckillOrderQueue() {
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    /**
     * 死信路由
     * @return
     */
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    /**
     * 绑定：交换机与订单解锁延迟队列
     * @return
     */
    @Bean
    public Binding orderCreateOrderBinding() {
        /**
         * String destination, 目的地（队列名或者交换机名字）
         * DestinationType destinationType, 目的地类型（Queue、Exhcange）
         * String exchange,
         * String routingKey,
         * Map<String, Object> arguments
         **/
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    /**
     * 绑定：交换机与订单解锁死信队列
     * @return
     */
    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    /**
     * 二次确认，保证订单关闭后库存能够正确解锁
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    @Bean
    public Binding orderSeckillOrderBinding() {
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }
}
