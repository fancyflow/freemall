package com.zqswjtu.freemall.order.to;

import com.zqswjtu.freemall.order.entity.OrderEntity;
import com.zqswjtu.freemall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;
}
