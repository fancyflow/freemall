package com.zqswjtu.freemall.order.dao;

import com.zqswjtu.freemall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:09:26
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    int updateOrderStatusByOrderSn(@Param("orderSn") String orderSn, @Param("status") Integer status);
}
