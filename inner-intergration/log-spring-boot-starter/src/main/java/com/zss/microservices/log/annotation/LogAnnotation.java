package com.zss.microservices.log.annotation;

import java.lang.annotation.*;

/**
 * 日志注解
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 17:07
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {

    /**
     * 模块
     * @return
     */
    String module();

    /**
     * 是否记录请求参数，默认是
     * @return
     */
    boolean recordRequestParam() default true;
}
