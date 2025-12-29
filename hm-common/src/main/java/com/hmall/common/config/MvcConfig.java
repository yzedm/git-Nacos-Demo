package com.hmall.common.config;


import com.hmall.common.Interceptors.UserInfoInterceptor;
import org.apache.catalina.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@ConditionalOnClass(DispatcherServlet.class)
/*ConditionalOnClass 该注解的作用是，如果该环境下没有springmvc的核心类DispatcherServlet的话
* 就不会自动将该MvcConfig加载，主要的作用是网关(没有springmvc)部分需要加载该类(有springmvc)*/
public class MvcConfig  implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //该拦截器不会拦截任何请求，只是为了传递Userinfo对象
        registry.addInterceptor(new UserInfoInterceptor());
    }
}
