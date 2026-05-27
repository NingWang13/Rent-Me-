package com.community.common.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersionAnnotation {
    String value() default "v1";
    boolean deprecated() default false;
    String deprecationMessage() default "此接口版本已废弃，请使用最新版本";
    String sunsetDate() default "";
}
