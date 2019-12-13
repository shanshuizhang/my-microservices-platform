package com.zss.microservices.log.util;

import com.zss.microservices.common.constant.TraceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 18:32
 * api
 * 经过filter-->  interceptor  -->aop  -->controller
 * 如果某些接口，比如filter --> userdetail
 * 这种情况，aop mdc设置  后续log输出traceid
 */
public class TraceUtil {
    public static String getTrace(){
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        String app_trace_id = request.getHeader(TraceConstant.HTTP_HEADER_TRACE_ID);

        if(StringUtils.isBlank(MDC.get(TraceConstant.LOG_TRACE_ID))){
            if(StringUtils.isNotEmpty(app_trace_id)){
                MDC.put(TraceConstant.LOG_TRACE_ID,app_trace_id);
            }
        }
        return app_trace_id;
    }
}
