package com.zqswjtu.freemall.order.listener;

import com.rabbitmq.client.Channel;
import com.zqswjtu.freemall.order.entity.OrderEntity;
import com.zqswjtu.freemall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void closeOrder(OrderEntity order, Message message, Channel channel) throws IOException {
        System.out.println("收到过期的订单消息，准备关闭订单：" + order.getOrderSn());
        try {
            orderService.closeOrder(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("关闭订单：" + order.getOrderSn() + "成功");
        } catch (Exception e) {
            System.out.println("关闭订单" + order.getOrderSn() + "失败，稍后将重试");
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
