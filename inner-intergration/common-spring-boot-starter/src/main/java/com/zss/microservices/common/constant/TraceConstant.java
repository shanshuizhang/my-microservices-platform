package com.zss.microservices.common.constant;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 17:32
 */
public class TraceConstant {

    /**
     * 日志跟踪id名。MDC中存储
     */
    public static final String LOG_TRACE_ID = "traceId";

    public static final String LOG_B3_TRACEID = "X-B3-TraceId";

    /**
     * 请求头跟踪id名。header中传递
     */
    public static final String HTTP_HEADER_TRACE_ID = "app_trace_id";
}
