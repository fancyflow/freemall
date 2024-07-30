package com.zqswjtu.freemall.ware.feign;

import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("freemall-order")
public interface OrderFeignService {
    @GetMapping("/order/order/getOrderStatus")
    R getOrderStatus(@RequestParam("orderSn") String orderSn);
}
