package com.zss.microservices.common.rest;

import com.zss.microservices.common.interceptor.RestTemplateInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/12 16:26
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(){
//		 长连接
        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager();
//		 总连接数
        pollingConnectionManager.setMaxTotal(1000);
//		同路由的并发数
        pollingConnectionManager.setDefaultMaxPerRoute(1000);

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setConnectionManager(pollingConnectionManager);
//		  重试次数，默认是3次，没有开启
//		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
        HttpClient httpClient = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                httpClient);
//		 连接超时
        clientHttpRequestFactory.setConnectTimeout(12000);
//		 数据读取超时时间，即SocketTimeout
        clientHttpRequestFactory.setReadTimeout(12000);
//		连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
        clientHttpRequestFactory.setConnectionRequestTimeout(200);
//		缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
//		clientHttpRequestFactory.setBufferRequestBody(false);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        //传递token traceid
        restTemplate.setInterceptors(
                Collections.singletonList(
                        new RestTemplateInterceptor()
                )
        );
        return restTemplate;
    }
}
