package com.zqswjtu.freemall.member.feign;

import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 这是一个声明式的远程调用
 */
@FeignClient("freemall-coupon")
public interface CouponFeignService {
    @RequestMapping("coupon/coupon/member/list")
    public R getMemberCoupons();
}
