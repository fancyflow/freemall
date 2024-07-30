package com.zqswjtu.freemall.order.interceptor;

import com.zqswjtu.common.vo.member.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 拦截器放行freemall-ware模块调用order模块的远程调用
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/**", uri);
        if (match) {
            return true;
        }

        MemberResponseVo user = (MemberResponseVo) request.getSession().getAttribute("loginUser");
        if (user != null) {
            // 表明已登录
            loginUser.set(user);
            return true;
        }
        request.getSession().setAttribute("msg", "请先进行登录");
        response.sendRedirect("http://auth.freemall.com/login.html");
        return false;
    }
}
