package com.zqswjtu.freemall.thirdparty.controller;

import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sms/")
public class SmsController {

    @Autowired
    private SmsComponent smsComponent;

    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("mobile") String mobile, @RequestParam("code") String code) {
        System.out.println("mobile: " + mobile);
        System.out.println("code: " + code);
        smsComponent.sendSmsCode(mobile, code);
        return R.ok();
    }
}
