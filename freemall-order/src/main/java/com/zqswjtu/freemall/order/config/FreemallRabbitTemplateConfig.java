package com.zqswjtu.freemall.order.config;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class FreemallRabbitTemplateConfig {
    // 解决循环依赖参考：https://blog.csdn.net/qq_41731316/article/details/119803796
    // 自动配置类加载顺序（外部类和静态内部类顺序）参考：https://www.cnblogs.com/yourbatman/p/13321589.html
    /**
     * 使用JSON序列化机制，进行消息转换
     * @return
     */
    @Bean
    public static MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 定制RabbitTemplate
     * 1、RabbitMQ服务器的交换机收到消息就回调
     *    1）配置spring.rabbitmq.publisher-confirm-type: correlated
     *    2）设置确认回调ConfirmCallback
     * 2、消息正确抵达RabbitMQ中交换机绑定的队列就回调
     *    1）配置spring.rabbitmq.publisher-returns: true
     *          spring.rabbitmq.template.mandatory: true
     *    2）设置确认回调ReturnsCallback
     * 3、消费端确认，保证每一个消息被正确消费，此时Broker才可以删除该消息
     *    1）默认是自动确认的，只要消息被接收到，客户端会自动确认，RabbitMQ服务端就会删除这个消息
     *    2）问题：客户端受到很多消息并且自动回复给RabbitMQ服务端ack，但是消息没有全部处理完成前就发生了一些故障会导致消息丢失
     *       手动确认模式，设置spring.rabbitmq.listener.simple.acknowledge-mode: manual
     */
    // RabbitTemplateConfig对象创建完成后执行这个方法
    @PostConstruct
    public void initRabbitTemplate() {
        // 设置确认回调函数
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达Broker，那么b就为true
             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id）
             * @param b 消息是否成功收到
             * @param s 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                System.out.println("confirm ... correlationData["+correlationData+"] ==> ack["+b+"] ==> cause["+s+"]");
            }
        });

        // 设置确认消息抵达队列的确认回调
        /**
         * 只要消息没有投递给指定的队列，就会触发这个失败回调
         * ReturnedMessage类的成员变量：
         *     private final Message message: 投递失败的消息详细信息
         *     private final int replyCode: 回复的状态码
         *     private final String replyText: 回复的文本内容
         *     private final String exchange: 当时这个消息发送给了哪个交换机
         *     private final String routingKey: 当时这个消息使用了哪个路由键
         */
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                System.out.println("fail ... message["+returnedMessage.getMessage()+"] ==> replyCode["+
                        returnedMessage.getReplyCode()+"] ==> replayText["+
                        returnedMessage.getReplyText()+"] ==> exchange["+
                        returnedMessage.getExchange()+"] ==> routingKey["+
                        returnedMessage.getRoutingKey()+"]");
            }
        });
    }
}
