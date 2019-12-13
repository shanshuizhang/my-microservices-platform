package com.zss.microservices.log.annotation;

import com.zss.microservices.log.selector.LogImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动日志框架支持
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 16:29
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LogImportSelector.class)
public @interface EnableLogging {
}
