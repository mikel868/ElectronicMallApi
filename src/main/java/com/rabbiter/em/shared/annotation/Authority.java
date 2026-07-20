package com.rabbiter.em.shared.annotation;

import com.rabbiter.em.system.entity.AuthorityType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface Authority {
    AuthorityType value() default AuthorityType.requireLogin;
}
