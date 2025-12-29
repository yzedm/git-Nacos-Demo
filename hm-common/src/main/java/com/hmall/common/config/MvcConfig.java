package com.hmall.common.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@ConditionalOnClass(DispatcherServlet.class)
/*ConditionalOnClass 该注解的作用是，如果该环境下没有springmvc的核心类DispatcherServlet的话
* 就不会自动将该MvcConfig加载，主要的作用是网关部分需要加载该类*/
public class MvcConfig  implements WebMvcConfigurer {

}
