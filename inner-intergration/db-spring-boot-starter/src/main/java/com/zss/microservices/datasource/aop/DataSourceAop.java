package com.zss.microservices.datasource.aop;

import com.zss.microservices.datasource.annotation.DataSource;
import com.zss.microservices.datasource.constant.DataSourceKey;
import com.zss.microservices.datasource.util.DataSourceHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/5 19:47
 */
@Slf4j
@Aspect
@Order(-1)
public class DataSourceAop {

    @Pointcut("@annotation(com.zss.microservices.datasource.annotation.DataSource)")
    public void dataSourcePointCut(){

    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        DataSource dataSource = method.getAnnotation(DataSource.class);
        if(dataSource == null){
            DataSourceHolder.setDataSourceKey(DataSourceKey.valueOf("core"));
        }else{
            DataSourceHolder.setDataSourceKey(DataSourceKey.valueOf(dataSource.name()));
        }

        try{
            return joinPoint.proceed();
        }finally{
            DataSourceHolder.clearDataSourceKey();
        }
    }

    @Before("@annotation(dataSource)")
    public void changeDataSource(JoinPoint joinPoint,DataSource dataSource){
        try{
            DataSourceHolder.setDataSourceKey(DataSourceKey.valueOf(dataSource.name()));
        }catch(Exception e){
            log.error("数据源[{}]不存在，使用默认数据源 > [{}]",dataSource.name(),joinPoint.getSignature());
        }
    }

    @After("@annotation(dataSource)")
    public void restoreDataSource(JoinPoint joinPoint,DataSource dataSource){
        log.debug("恢复数据源：[{}] > [{}]",dataSource.name(),joinPoint.getSignature());
        DataSourceHolder.clearDataSourceKey();
    }
}
