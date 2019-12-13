package com.zss.microservices.log.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.zss.microservices.common.auth.details.LoginAppUser;
import com.zss.microservices.common.constant.TraceConstant;
import com.zss.microservices.common.model.SysLog;
import com.zss.microservices.common.util.SysUserUtil;
import com.zss.microservices.log.annotation.LogAnnotation;
import com.zss.microservices.log.service.LogService;
import com.zss.microservices.log.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 18:01
 * 日志AOP,标准日志格式logback-spring.xml
 * 如果开启日志记录，需要多数据配置
 */
@Slf4j
@Aspect
@Order(-1)
public class LogAnnotationAop {

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    private LogService logService;

    @Around("@annotation(logAnnotation)")
    public Object logSave(ProceedingJoinPoint joinPoint,LogAnnotation logAnnotation) throws Throwable {

        //获取请求流水号
        String transid = StringUtils.defaultString(TraceUtil.getTrace(), MDC.get(TraceConstant.LOG_TRACE_ID));
        // 记录开始时间
        long start = System.currentTimeMillis();
        // 获取方法参数
        String url = null;
        String httpMethod = null;
        Object result = null;
        List<Object> httpReqArgs = new ArrayList<>();

        SysLog sysLog = new SysLog();
        sysLog.setCreateTime(new Date());

        LoginAppUser loginAppUser = SysUserUtil.getLoginAppUser();
        if (loginAppUser != null) {
            sysLog.setUsername(loginAppUser.getUsername());
        }

        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        LogAnnotation annotation = method.getDeclaredAnnotation(LogAnnotation.class);
        sysLog.setModule(annotation.module() + ":" + methodSignature.getDeclaringTypeName() + "/" + methodSignature.getName());

        String params = null;
        url = methodSignature.getDeclaringTypeName() + "/" + methodSignature.getName();
        Object[] args = joinPoint.getArgs();
        for(Object object:args){
            if(object instanceof HttpServletRequest){
                HttpServletRequest request = (HttpServletRequest) object;
                url = request.getRequestURI();
                httpMethod = request.getMethod();
            }else if(object instanceof HttpServletResponse){
            }else{
                httpReqArgs.add(object);
            }
        }

        try{
            params = JSONObject.toJSONString(httpReqArgs);
            sysLog.setParams(params);
            // 打印请求参数参数
            log.info("开始请求，transid={},  url={} , httpMethod={}, reqData={} ", transid, url, httpMethod, params);
        } catch (Exception e) {
            log.error("记录参数失败：{}", e.getMessage());
        }

        try {
            // 调用原来的方法
            result = joinPoint.proceed();
            sysLog.setFlag(Boolean.TRUE);
        } catch (Exception e) {
            sysLog.setFlag(Boolean.FALSE);
            sysLog.setRemark(e.getMessage());
            log.error("请求报错，transid={},  url={} , httpMethod={}, reqData={} ,error ={} ", transid, url, httpMethod, params,e.getMessage());
            throw e;
        } finally {
            CompletableFuture.runAsync(() -> {
                try {
                    if (logAnnotation.recordRequestParam()) {
                        log.trace("日志落库开始：{}", sysLog);
                        if(logService!=null){
                            logService.save(sysLog);
                        }
                        log.trace("开始落库结束：{}", sysLog);
                    }
                } catch (Exception e) {
                    log.error("落库失败：{}", e.getMessage());
                }
            }, new TraceableExecutorService(beanFactory, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) ,
                    // 'calculateTax' explicitly names the span - this param is optional
                    "logAop"));

            // 获取回执报文及耗时
            log.info("请求完成, transid={}, 耗时={}, resp={}:", transid, (System.currentTimeMillis() - start),
                    result == null ? null : JSON.toJSONString(result));

        }
        return result;
    }
}
