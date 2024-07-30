package com.zqswjtu.freemall.search.feign;

import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("freemall-product")
public interface ProductFeignService {

    @GetMapping("product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);
}
