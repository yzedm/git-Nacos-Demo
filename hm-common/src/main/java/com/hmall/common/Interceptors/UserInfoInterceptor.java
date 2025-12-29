package com.hmall.common.Interceptors;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    //该拦截器不做任何的登录拦截功能，唯一的作用就是保存登录成功的user用户信息

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在进入controller前执行，所以他的作用是保存用户信息
        //1.获取登录用户信息
        String header = request.getHeader("user-info");
        //2.判断是否获取了用户，存入ThreadLocal
        if (StrUtil.isNotBlank(header)) {
            UserContext.setUser(Long.valueOf(header));
        }
        //3.放行
            return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //controller之后执行，清理用户信息
        UserContext.removeUser();

    }}
