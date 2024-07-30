package com.zqswjtu.freemall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.zqswjtu.common.enums.OrderStatusEnum;
import com.zqswjtu.common.to.mq.OrderTo;
import com.zqswjtu.common.to.mq.StockDetailTo;
import com.zqswjtu.common.to.mq.StockLockedTo;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.ware.dao.WareSkuDao;
import com.zqswjtu.freemall.ware.entity.WareOrderTaskDetailEntity;
import com.zqswjtu.freemall.ware.entity.WareOrderTaskEntity;
import com.zqswjtu.freemall.ware.feign.OrderFeignService;
import com.zqswjtu.freemall.ware.service.WareOrderTaskDetailService;
import com.zqswjtu.freemall.ware.service.WareOrderTaskService;
import com.zqswjtu.freemall.ware.service.WareSkuService;
import com.zqswjtu.freemall.ware.vo.OrderVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service("stockReleaseListener")
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 库存自动解锁
     * 只要解锁库存的消息执行失败，一定要告诉服务解锁失败，并且不能删除该消息
     * @param to
     * @param message
     */
    @RabbitHandler
    private void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息...");
        // 没有捕获到异常说明库存解锁成功，直接手动给消息队列发送确认
        try {
            wareSkuService.unLockStock(to);
            // 回复确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("库存解锁成功...");
        } catch (Exception e) {
            System.out.println("库存解锁失败，稍后将重试...");
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    private void handleOrderCloseRelease(OrderTo order, Message message, Channel channel) throws IOException {
        System.out.println("收到订单关闭的消息，准备解锁库存...");
        try {
            wareSkuService.unLockStock(order);
            System.out.println("库存解锁成功...");
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.out.println("库存解锁失败，稍后将重试...");
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
