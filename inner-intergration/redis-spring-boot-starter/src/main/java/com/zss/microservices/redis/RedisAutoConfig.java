package com.zss.microservices.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 9:51
 */
@Configuration
public class RedisAutoConfig {

    @Bean
    public RedisTemplate redisTemplate(){
        return new RedisTemplate();
    }
}
