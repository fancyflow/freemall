package com.zqswjtu.freemall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.exception.NoStockException;
import com.zqswjtu.common.to.mq.SeckillOrderTo;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.order.entity.OrderEntity;
import com.zqswjtu.freemall.order.vo.OrderConfirmVo;
import com.zqswjtu.freemall.order.vo.OrderSubmitVo;
import com.zqswjtu.freemall.order.vo.SubmitOrderResponseVo;

import java.util.Map;

/**
 * 订单
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:09:26
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder(MemberResponseVo memberResponseVo);

    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) throws NoStockException;

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity order);

    void payOrder(String orderSn);

    void createSeckillOrder(SeckillOrderTo order);
}

