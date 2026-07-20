package com.rabbiter.em.config;

import com.rabbiter.em.shared.interceptor.AuthorityInterceptor;
import com.rabbiter.em.shared.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Resource
    JwtInterceptor jwtInterceptor;
    @Resource
    AuthorityInterceptor authorityInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //jwt拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login","/register","/file/**","/avatar/**","/api/icon/**","/api/category","/api/market/**","/api/carousel/**","/api/seckill/active","/api/seckill/list")
                .order(0)
        ;
        //权限校验拦截器
        registry.addInterceptor(authorityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns()
                .order(1)
        ;

        WebMvcConfigurer.super.addInterceptors(registry);
    }


}