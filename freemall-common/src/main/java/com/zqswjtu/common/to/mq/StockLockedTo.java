package com.zqswjtu.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {
    private Long id; // 每一个成功保存的订单都会生成一个对应的库存工作单号id
    private StockDetailTo detailTo; // 库存工作单详情id
}
