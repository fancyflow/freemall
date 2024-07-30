package com.zqswjtu.freemall.seckill.to;

import com.zqswjtu.freemall.seckill.vo.SeckillSkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisTo {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    // 商品秒杀随机码
    private String randomCode;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    // sku的详细信息
    private SeckillSkuInfoVo skuInfo;

    // 当前商品秒杀的开始和结束时间
    private Long startTime;
    private Long endTime;
}
