package com.zqswjtu.freemall.order.web;

import com.zqswjtu.freemall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

@Controller
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public OrderEntity createOrderTest() {
        OrderEntity order = new OrderEntity();
        order.setOrderSn(UUID.randomUUID().toString());
        order.setModifyTime(new Date());

        System.out.println("创建订单" + order.getOrderSn() + "时间：" + order.getModifyTime());
        // 给MQ发消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order);
        return order;
    }
}
