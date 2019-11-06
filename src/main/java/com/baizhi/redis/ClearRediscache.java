package com.baizhi.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  //指明当前使用位置
@Retention(RetentionPolicy.RUNTIME)  //指明当前注解生效时机，运行时生效
public @interface ClearRediscache {
}
