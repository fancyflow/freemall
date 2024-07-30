package com.zqswjtu.freemall.order.dao;

import com.zqswjtu.freemall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:09:26
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
