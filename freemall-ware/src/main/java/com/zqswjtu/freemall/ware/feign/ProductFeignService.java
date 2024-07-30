package com.zqswjtu.freemall.ware.feign;

import com.zqswjtu.common.to.SkuNameTo;
import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("freemall-product")
public interface ProductFeignService {
    @PostMapping("/product/skuinfo/getSkuName")
    R getSkuName(@RequestBody SkuNameTo skuNameTo);
}
