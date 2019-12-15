package com.zss.microservices.uaa.server.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;

import java.util.concurrent.TimeUnit;

/**
 * 替换默认的JdbcAuthorizationCodeServices存储授权码策略
 */
public class RedisAuthorizationCodeServices extends RandomValueAuthorizationCodeServices {

    private RedisTemplate<String,Object> redisTemplate;

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 替换JdbcAuthorizationCodeServices的存储策略
     * 将存储code到redis，并设置过期时间，10分钟<br>
     * @param code
     * @param authentication
     */
    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        redisTemplate.opsForValue().set(redisKey(code),authentication,10L,TimeUnit.MINUTES);
    }

    @Override
    protected OAuth2Authentication remove(final String code) {
        String codeKey = redisKey(code);
        OAuth2Authentication authentication = (OAuth2Authentication)redisTemplate.opsForValue().get(codeKey);
        redisTemplate.delete(codeKey);
        return authentication;
    }

    /**
     * redis中 code key的前缀
     * @param code
     * @return
     */
    private String redisKey(String code){
        return "oauth:code:" + code;
    }
}
