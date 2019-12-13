package com.zss.microservices.log.interceptor;

import com.zss.microservices.common.constant.TraceConstant;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 首先创建拦截器，加入拦截列表中，在请求到达时生成traceId。
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 17:36
 */
public class LogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader(TraceConstant.HTTP_HEADER_TRACE_ID);
        if(StringUtils.hasLength(traceId)) {
            MDC.put(TraceConstant.LOG_TRACE_ID,traceId);
        }
        return true;
    }
}
