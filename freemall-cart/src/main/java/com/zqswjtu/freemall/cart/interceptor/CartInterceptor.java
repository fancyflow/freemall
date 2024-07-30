package com.zqswjtu.freemall.cart.interceptor;

import com.zqswjtu.common.constant.CartConstant;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.cart.vo.UserInfoVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoVo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoVo userInfoVo = new UserInfoVo();
        HttpSession session = request.getSession();
        MemberResponseVo member = (MemberResponseVo) session.getAttribute("loginUser");
        if (member != null) {
            userInfoVo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(name)) {
                    userInfoVo.setUserKey(cookie.getValue());
                    userInfoVo.setTempUser(true);
                }
            }
        }
        // 如果没有，临时用户一定会被分配一个uuid作为标识记用户的唯一字段，可以存放到浏览器cookie中
        if (StringUtils.isEmpty(userInfoVo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoVo.setUserKey(uuid);
        }
        threadLocal.set(userInfoVo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoVo userInfoVo = threadLocal.get();
        if (userInfoVo != null && !userInfoVo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoVo.getUserKey());
            cookie.setDomain("freemall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_EXPIRATION_TIME);
            response.addCookie(cookie);
        }
    }
}
