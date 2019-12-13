package com.zss.microservices.log.selector;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 17:00
 * log-spring-boot-starter 自动装配
 */
public class LogImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{
                "com.zss.microservices.log.aop.LogAnnotationAop",
                "com.zss.microservices.log.service.impl.LogServiceImpl",
                "com.zss.microservices.log.config.LogAutoConfig"
        };
    }
}
