package com.ym.api.cfg;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;


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
