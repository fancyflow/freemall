package com.zqswjtu.freemall.order.listener;

import com.rabbitmq.client.Channel;
import com.zqswjtu.common.to.mq.SeckillOrderTo;
import com.zqswjtu.freemall.order.entity.OrderEntity;
import com.zqswjtu.freemall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues = "order.seckill.order.queue")
@Service
public class SeckillOrderCreateListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void createSeckillOrder(SeckillOrderTo order, Message message, Channel channel) throws IOException {
        System.out.println("收到创建秒杀订单的消息，准备创建订单：" + order.getOrderSn());
        try {
            orderService.createSeckillOrder(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("创建订单：" + order.getOrderSn() + "成功");
        } catch (Exception e) {
            System.out.println("创建订单" + order.getOrderSn() + "失败，稍后将重试");
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
