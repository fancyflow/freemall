package com.zqswjtu.freemall.order.feign;

import com.zqswjtu.freemall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("freemall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    List<OrderItemVo> getCurrentUserCartItems(@RequestParam("userId") Long userId);

    @GetMapping("/deletePayedCartItems")
    @ResponseBody
    Integer deletePayedCartItems(@RequestParam("userId") Long userId);
}
