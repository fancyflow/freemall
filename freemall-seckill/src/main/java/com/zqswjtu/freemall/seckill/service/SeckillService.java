package com.zqswjtu.freemall.seckill.service;

import com.zqswjtu.freemall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatestThreeDays();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfoBySkuId(Long skuId);

    String kill(String killId, String key, Integer num);
}
