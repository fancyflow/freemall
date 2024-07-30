package com.zqswjtu.freemall.order.feign;

import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("freemall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/getSpuInfoBySkuId")
    R getSpuInfoBySkuId(@RequestParam("skuId") Long skuId);
}
