package com.zqswjtu.freemall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zqswjtu.common.constant.SeckillConstant;
import com.zqswjtu.common.to.mq.SeckillOrderTo;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.seckill.feign.CouponFeignService;
import com.zqswjtu.freemall.seckill.feign.ProductFeignService;
import com.zqswjtu.freemall.seckill.interceptor.LoginInterceptor;
import com.zqswjtu.freemall.seckill.service.SeckillService;
import com.zqswjtu.freemall.seckill.to.SeckillSkuRedisTo;
import com.zqswjtu.freemall.seckill.vo.SeckillSessionsWithSkus;
import com.zqswjtu.freemall.seckill.vo.SeckillSkuInfoVo;
import com.zqswjtu.freemall.seckill.vo.SeckillSkuRelationVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatestThreeDays() {
        // 1、扫描需要参与秒杀活动的商品
        R r = couponFeignService.getLatestThreeDaysSeckillSession();
        if (r.getCode() == 0) {
            // 上架秒杀商品
            List<SeckillSessionsWithSkus> seckillSessionsData =
                    r.getData(new TypeReference<List<SeckillSessionsWithSkus>>(){});
            // 将秒杀商品存储在redis中
            // 缓存秒杀活动信息
            saveSessionsInfo(seckillSessionsData);
            // 缓存秒杀活动关联的商品信息
            saveSeckillSessionsInfo(seckillSessionsData);
        }
    }

    /**
     * 获取当前可以参与秒杀活动的商品信息
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // 1、确定当前时间属于哪个秒杀场次
        long time = System.currentTimeMillis();
        Set<String> keys = stringRedisTemplate.keys(SeckillConstant.SECKILL_SESSION_CACHE_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String timeString = key.substring(key.lastIndexOf(":") + 1);
                String[] s = timeString.split("_");
                long startTime = Long.parseLong(s[0]);
                long endTime = Long.parseLong(s[1]);
                if (startTime <= time && time <= endTime) {
                    // 2、获取当前秒杀场次的所有商品信息
                    List<String> range = stringRedisTemplate.opsForList().range(key, 0, -1);
                    BoundHashOperations<String, String, Object> ops = stringRedisTemplate.boundHashOps(SeckillConstant.SECKILL_SKUS_CACHE_PREFIX);
                    List<Object> objects = ops.multiGet(range);
                    if (objects != null) {
                        List<SeckillSkuRedisTo> collect = objects.stream()
                                .map(item -> JSON.parseObject((String) item, SeckillSkuRedisTo.class))
                                .collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        }
        return null;
    }

    /**
     * 获取某个sku商品是否参与秒杀活动
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfoBySkuId(Long skuId) {
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SeckillConstant.SECKILL_SKUS_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null) {
            for (String key : keys) {
                Long id = Long.parseLong(key.substring(key.indexOf('_') + 1));
                if (skuId.equals(id)) {
                    String json = ops.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    long current = System.currentTimeMillis();
                    assert seckillSkuRedisTo != null;
                    if (seckillSkuRedisTo.getStartTime() <= current && current <= seckillSkuRedisTo.getEndTime()) {
                    } else {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        MemberResponseVo user = LoginInterceptor.loginUser.get();
        assert user != null;
        // 获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SeckillConstant.SECKILL_SKUS_CACHE_PREFIX);
        String json = ops.get(killId);
        if (!StringUtils.isEmpty(json)) {
            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            // 校验合法性
            // 校验时间是否过期
            Long startTime = seckillSkuRedisTo.getStartTime();
            Long endTime = seckillSkuRedisTo.getEndTime();
            long current = System.currentTimeMillis();
            if (startTime <= current && current <= endTime) {
                // 校验随机码
                Long skuId = seckillSkuRedisTo.getSkuId();
                Long promotionSessionId = seckillSkuRedisTo.getPromotionSessionId();
                String randomCode = seckillSkuRedisTo.getRandomCode();
                if (randomCode.equals(key) && killId.equals(promotionSessionId + "_" + skuId)) {
                    // 验证购物数量是否合理
                    if (num <= seckillSkuRedisTo.getSeckillLimit().intValue()) {
                        // 验证该用户是否已经购买过，即幂等性处理
                        // 如果某个用户秒杀成功就去redis中占位，key：用户id_秒杀场次id_商品id
                        String placeHolderKey = user.getId() + "_" + promotionSessionId + "_" + skuId;
                        // setnx命令，设置成功说明没有购买过
                        long ttl = endTime - current;
                        Boolean flag = stringRedisTemplate.opsForValue()
                                .setIfAbsent(placeHolderKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (Boolean.TRUE.equals(flag)) {
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SECKILL_SKU_STOCK_SEMAPHORE + randomCode);
                            boolean acquire = semaphore.tryAcquire(num);
                            if (acquire) {
                                String orderSn = IdWorker.getTimeId();
                                SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                seckillOrderTo.setOrderSn(orderSn);
                                seckillOrderTo.setPromotionSessionId(promotionSessionId);
                                seckillOrderTo.setSkuId(skuId);
                                seckillOrderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
                                seckillOrderTo.setNum(num);
                                seckillOrderTo.setMemberId(user.getId());
                                // 秒杀成功发送给消息队列，让消息队列自动生成订单
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                return orderSn;
                            } else {
                                // 秒杀失败删除占位的key
                                stringRedisTemplate.delete(placeHolderKey);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void saveSessionsInfo(List<SeckillSessionsWithSkus> sessions) {
        if (sessions == null) {
            return;
        }
        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SeckillConstant.SECKILL_SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            // 缓存秒杀活动信息
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(key))) {
                // 缓存中没有该key才保存活动信息
                List<String> skuIds = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString())
                        .collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }

    private void saveSeckillSessionsInfo(List<SeckillSessionsWithSkus> sessions) {
        if (sessions == null) {
            return;
        }
        sessions.forEach(session -> {
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SeckillConstant.SECKILL_SKUS_CACHE_PREFIX);
            session.getRelationSkus().forEach(seckillSkuRelationVo -> {
                String key = seckillSkuRelationVo.getPromotionSessionId()
                        + "_"
                        + seckillSkuRelationVo.getSkuId().toString();
                if (Boolean.FALSE.equals(ops.hasKey(key))) {
                    // 缓存商品
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    // 1、sku的基本信息
                    R r = productFeignService.getSkuInfo(seckillSkuRelationVo.getSkuId());
                    if (r.getCode() == 0) {
                        SeckillSkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SeckillSkuInfoVo>(){});
                        seckillSkuRedisTo.setSkuInfo(skuInfo);
                    }

                    // 2、sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuRelationVo, seckillSkuRedisTo);

                    // 3、设置当前秒杀商品的时间信息
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    // 4、设置商品的随机码
                    String token = UUID.randomUUID().toString().replace("-", "");
                    seckillSkuRedisTo.setRandomCode(token);

                    // 商品可以秒杀的数量作为信号量限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SECKILL_SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(seckillSkuRelationVo.getSeckillCount().intValue());

                    String jsonString = JSON.toJSONString(seckillSkuRedisTo);
                    ops.put(key, jsonString);
                }
            });
        });
    }
}
