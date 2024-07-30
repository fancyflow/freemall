package com.zqswjtu.freemall.seckill.feign;

import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("freemall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/latestThreeDaysSeckillSession")
    R getLatestThreeDaysSeckillSession();
}
