package com.zqswjtu.freemall.order.vo;

import com.zqswjtu.freemall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code; // 0代表成功
}
