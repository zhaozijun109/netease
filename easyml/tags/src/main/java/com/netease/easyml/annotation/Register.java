package com.netease.easyml.annotation;

import java.lang.annotation.*;

/**
 * Created by linjiuning on 2020/7/6.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Register {
    String name() default "";

    String prefix() default "";

    String[] alias() default {};

    Class<?> parent() default Object.class;

    boolean isDefault() default false;

    boolean existOk() default false;
}
