package com.hmdp.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户登录的拦截器
 *
 * @author 21027
 * @date 2022/8/18 23:53
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否需要拦截（ThreadLocal中是否存在用户）
        if (UserHolder.getUser() == null) {
            //没有，需要拦截，设置状态
            response.setStatus(401);
            //拦截
            return false;
        }
        //有用户，放行
        return true;
    }
}
