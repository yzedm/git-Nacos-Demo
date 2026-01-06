package com.ym.api.cfg;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;


//通过拦截器 来传递user的登录信息
public class DefaultFeignConfig {
        @Bean
        public Logger.Level feignLoggerLevel(){
            return Logger.Level.FULL;
        }
        @Bean
        public RequestInterceptor userInterceptor(){
            return new RequestInterceptor() {
                @Override
                public void apply(RequestTemplate requestTemplate){
                    Long UserId =UserContext.getUser();
                    if(UserId!=null){
                        requestTemplate.header("user-info", UserId.toString());
                    }
                }
            };
        }
}
