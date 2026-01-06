package com.ym.gateway.filters;


import cn.hutool.core.util.StrUtil;
import com.hmall.common.exception.UnauthorizedException;
import com.ym.gateway.config.AuthProperties;
import com.ym.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthLoginGloableFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher =  new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取request
        ServerHttpRequest request = exchange.getRequest();
        //2.判断是否需要登录校验
        if(isExclude(request.getPath().toString())){
            //放行
            return chain.filter(exchange);
        }
        //3.获取token
        String Token =null;
        List<String> headers = request.getHeaders().get("authorization");
        if(headers != null && !headers.isEmpty()){
            Token = headers.get(0);
        }
        Long userid = null;
        //校验token
        try {
            userid = jwtTool.parseToken(Token);

        } catch (UnauthorizedException e) {
            //拦截，设置响应码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应终止
              return  response.setComplete();
        }
        //传递用户信息,将用户信息保存到请求头中，但是不是每个微服务都需要用户信息
        String userInfo = userid.toString();
        ServerWebExchange newExchange = exchange.mutate() // mutate就是对下游的请求做处理
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        //将包含 user-info的信息传递下去
        return chain.filter(newExchange);
    }

    private boolean isExclude(String path) {
        // spring提供的路径匹配器
        for (String excludePath : authProperties.getExcludePaths()) {
            if(antPathMatcher.match(excludePath,path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
