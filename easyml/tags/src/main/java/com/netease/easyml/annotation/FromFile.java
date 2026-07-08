package com.netease.easyml.annotation;

import java.lang.annotation.*;

/**
 * Created by linjiuning on 2020/7/6.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FromFile {
    String type() default "";
}
