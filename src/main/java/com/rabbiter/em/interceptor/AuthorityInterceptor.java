package com.rabbiter.em.interceptor;

import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("/".equals(request.getRequestURI())) {
            return true;
        }
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            Class<?> clazz = handlerMethod.getBeanType();
            if(method!=null && clazz!=null){
                boolean isMethodAnnotation = method.isAnnotationPresent(Authority.class);
                boolean isClassAnnotation = clazz.isAnnotationPresent(Authority.class);
                Authority authority = null;
                //方法注解会覆盖类注解
                if(isMethodAnnotation){
                    authority = method.getAnnotation(Authority.class);
                }else if (isClassAnnotation){
                    authority = clazz.getAnnotation(Authority.class);
                }
                if(authority==null){
                    return true;
                }
                // JwtInterceptor已经完成验证，用户信息在UserHolder中，直接判断即可
                User user = UserHolder.getUser();
                switch (authority.value()){
                    case requireLogin:
                        if (user == null) {
                            throw new ServiceException(Constants.CODE_401, "登录状态失效！");
                        }
                        return true;
                    case requireAuthority:
                        if (user == null) {
                            throw new ServiceException(Constants.CODE_401, "登录状态失效！");
                        }
                        if (!"admin".equals(user.getRole())) {
                            throw new ServiceException(Constants.CODE_403, "无权限！");
                        }
                        return true;
                    case noRequire:
                        return true;
                }
            }
        }
        return true;
    }
}
