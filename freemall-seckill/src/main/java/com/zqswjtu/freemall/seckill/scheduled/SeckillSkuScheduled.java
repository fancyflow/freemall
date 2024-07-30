package com.zqswjtu.freemall.seckill.scheduled;

import com.zqswjtu.common.constant.SeckillConstant;
import com.zqswjtu.freemall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 秒杀商品定时上架
 * 每天凌晨3：00上架最近三天需要秒杀的商品
 * 当天00：00：00 ~ 23：59：59
 * 明天00：00：00 ~ 23：59：59
 * 后天00：00：00 ~ 23：59：59
 */
@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    // TODO 幂等性处理，防止秒杀商品重复上架
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatestThreeDays() {
        // 1、重复上架无需处理
        log.info("上架秒杀的商品信息...");
        RLock lock = redissonClient.getLock(SeckillConstant.UPLOAD_LOCK);
        lock.lock();
        try {
            seckillService.uploadSeckillSkuLatestThreeDays();
        } finally {
            lock.unlock();
        }
    }
}
