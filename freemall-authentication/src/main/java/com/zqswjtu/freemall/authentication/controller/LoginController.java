package com.zqswjtu.freemall.authentication.controller;

import com.alibaba.fastjson.TypeReference;
import com.zqswjtu.common.constant.AuthenticationConstant;
import com.zqswjtu.common.exception.BizCodeEnum;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.authentication.feign.MemberFeignService;
import com.zqswjtu.freemall.authentication.feign.ThirdPartyFeignService;
import com.zqswjtu.freemall.authentication.util.RandomIntegerCodeUtils;
import com.zqswjtu.freemall.authentication.vo.UserLoginVo;
import com.zqswjtu.freemall.authentication.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes model, HttpSession session) {
        R r = memberFeignService.login(userLoginVo);
        if (r.getCode() != 0) {
            // 登录错误
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String) r.get("msg"));
            model.addFlashAttribute("errors", errors);
            return "redirect:http://auth.freemall.com/login.html";
        }
        MemberResponseVo loginUser = r.getData(new TypeReference<MemberResponseVo>() {});
        session.setAttribute("loginUser", loginUser);
        return "redirect:http://freemall.com";
    }

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("mobile") String mobile) {
        // 1、接口防刷
        String s = stringRedisTemplate.opsForValue().get(AuthenticationConstant.SMS_CODE_CACHE_PREFIX + mobile);
        if (s != null) {
            long start = Long.parseLong(s.split("_")[1]);
            if (System.currentTimeMillis() - start < 60000) {
                // 60秒内不能再发验证码
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
            // 还可以做一个拓展，超过5次出现获取验证码频率过高错误，将该手机号封号1天
        }

        // 用字母发验证码可能收不到
        // String code = UUID.randomUUID().toString().substring(0, 5);
        String code = RandomIntegerCodeUtils.getCode(AuthenticationConstant.SMS_CODE_LENGTH);

        // 2、验证码有效期校验，redis缓存，key：mobile，value：code
        // sms:code:18370588582 -> 12345_系统时间
        stringRedisTemplate.opsForValue()
                .set(AuthenticationConstant.SMS_CODE_CACHE_PREFIX + mobile,
                        code + "_" + System.currentTimeMillis(),
                        AuthenticationConstant.SMS_CODE_EXPIRATION_TIME,
                        AuthenticationConstant.SMS_CODE_EXPIRATION_TIME_UNIT);
        thirdPartyFeignService.sendCode(mobile, code);
        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo, BindingResult result,
                           RedirectAttributes model, HttpSession session) {
        if (result.hasErrors()) {
            // 校验出错则重新转发到注册页
            Map<String, String> errors = result.getFieldErrors().stream().collect(
                    Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            model.addFlashAttribute("errors", errors);
            return "redirect:http://auth.freemall.com/reg.html";
        }
        String code = userRegisterVo.getCode();
        String mobile = userRegisterVo.getMobile();
        String key = AuthenticationConstant.SMS_CODE_CACHE_PREFIX + mobile;
        String s = stringRedisTemplate.opsForValue().get(key);
        if (s == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码已过期");
            model.addFlashAttribute("error", errors);
            return "redirect:http://auth.freemall.com/reg.html";
        }
        if (s.split("_")[0].equals(code)) {
            // 验证码通过，删除验证码后调用远程服务进行用户注册
            stringRedisTemplate.delete(key);
            R r = memberFeignService.register(userRegisterVo);
            if (r.getCode() != 0) {
                // 注册失败
                Map<String, String> errors = new HashMap<>();
                errors.put("msg", (String) r.get("msg"));
                model.addFlashAttribute("errors", errors);
                return "redirect:http://auth.freemall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            model.addFlashAttribute("error", errors);
            return "redirect:http://auth.freemall.com/reg.html";
        }
        return "redirect:http://auth.freemall.com/login.html";
    }
}
