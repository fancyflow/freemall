package com.zqswjtu.freemall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交数据的vo
 */
@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    // 提交订单时无须提交需要购买的商品，而是去存储在redis里的购物车中再获取一遍被选中的数据
    private String uniqueToken; // 防重令牌
    private BigDecimal payPrice;
    private String note; // 订单备注信息
    // 直接去session中取出用户登录信息
}
