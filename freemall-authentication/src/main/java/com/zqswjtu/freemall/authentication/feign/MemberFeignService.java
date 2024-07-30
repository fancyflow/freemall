package com.zqswjtu.freemall.authentication.feign;

import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.authentication.vo.UserLoginVo;
import com.zqswjtu.freemall.authentication.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("freemall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo registerVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo loginVo);
}
