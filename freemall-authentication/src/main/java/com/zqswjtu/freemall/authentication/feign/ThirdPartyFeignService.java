package com.zqswjtu.freemall.authentication.feign;

import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("freemall-third-party")
public interface ThirdPartyFeignService {
    @GetMapping("sms/sendCode")
    public R sendCode(@RequestParam("mobile") String mobile, @RequestParam("code") String code);
}
