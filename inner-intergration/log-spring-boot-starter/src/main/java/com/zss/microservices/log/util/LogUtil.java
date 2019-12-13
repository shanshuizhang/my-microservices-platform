package com.zss.microservices.log.util;

import com.zss.microservices.common.constant.TraceConstant;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 20:01
 * 日志埋点工具类
 */
public class LogUtil {

    public static TraceConstant traceConstant;

    /**
     * 生成日志随机数
     *
     * @return
     */
    public static String getTraceId() {
        int i = 0;
        StringBuilder st = new StringBuilder();
        while (i < 5) {
            i++;
            st.append(ThreadLocalRandom.current().nextInt(10));
        }
        return st.toString() + System.currentTimeMillis();
    }
}

