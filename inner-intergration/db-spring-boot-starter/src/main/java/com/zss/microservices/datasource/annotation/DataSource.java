package com.zss.microservices.datasource.annotation;

import java.lang.annotation.*;

/**
 * 多数据源注解
 * <p/>
 * 指定要使用的数据源
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/5 19:43
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    //数据库名称
    String name();
}
