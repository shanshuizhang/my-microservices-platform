package com.zss.microservices.common.feign;

import cn.hutool.core.util.StrUtil;
import com.zss.microservices.common.constant.TraceConstant;
import com.zss.microservices.common.constant.UaaConstant;
import com.zss.microservices.common.util.StringUtils;
import com.zss.microservices.common.util.TokenUtil;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/12 19:24
 */
@Configuration
public class FeignInterceptorConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){

        RequestInterceptor requestInterceptor = (requestTemplate)->{
            //传递token
            //使用feign client访问别的微服务时，将accessToken header
            //config.anyRequest().permitAll() 非强制校验token
            if(StringUtils.isNotBlank(TokenUtil.getToken())){
                requestTemplate.header(UaaConstant.TOKEN_HEADER, TokenUtil.getToken() );
            }

            //传递traceId
            String traceId = StrUtil.isNotEmpty(MDC.get(TraceConstant.LOG_TRACE_ID))  ?  MDC.get(TraceConstant.LOG_TRACE_ID) :  MDC.get(TraceConstant.LOG_B3_TRACEID) ;
            if (StrUtil.isNotEmpty(traceId)) {
                requestTemplate.header(TraceConstant.HTTP_HEADER_TRACE_ID, traceId);
            }
        };

        return requestInterceptor;
    }
}
