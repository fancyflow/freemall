package com.zqswjtu.freemall.product.feign;

import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("freemall-seckill")
public interface SeckillFeignService {
    @GetMapping("/getSkuSeckillInfo")
    R getSkuSeckillInfo(@RequestParam("skuId") Long skuId);
}
