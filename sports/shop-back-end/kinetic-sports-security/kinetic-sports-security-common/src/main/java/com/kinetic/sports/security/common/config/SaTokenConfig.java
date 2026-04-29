package com.kinetic.sports.security.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/wxLogin",
                        "/auth/register",
                        "/auth/smsLogin",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/favicon.ico",
                        "/course/list",
                        "/course/detail/**",
                        "/course/location/list",
                        "/course/location/options",
                        "/course/schedule/list",
                        "/course/schedule/upcoming",
                        "/course/category/list",
                        "/coach/list",
                        "/coach/detail/**",
                        "/prod/list",
                        "/prod/detail/**",
                        "/prod/category/list",
                        "/prod/sku/list",
                        "/recommend/home",
                        "/recommend/course",
                        "/recommend/course/related/**",
                        "/recommend/prod",
                        "/recommend/prod/related/**",
                        "/banner/list",
                        "/ai/chat",
                        "/ai/session/**",
                        "/uploads/**",
                        "/sms/send"
                );
    }
}
