package com.community.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {

    String key();

    int ttl() default 300;

    String dataLevel() default "NORMAL";
}
