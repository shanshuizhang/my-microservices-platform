package com.zss.microservices.common.util;

import com.zss.microservices.common.auth.details.LoginAppUser;
import com.zss.microservices.common.constant.UaaConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Map;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/11 15:05
 */
public class SysUserUtil {

    /**
     * 获取登陆的 LoginAppUser
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static LoginAppUser getLoginAppUser() {

        // 当OAuth2AuthenticationProcessingFilter设置当前登录时，直接返回
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            OAuth2Authentication oAuth2Auth = (OAuth2Authentication) authentication;
            authentication = oAuth2Auth.getUserAuthentication();

            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
                return (LoginAppUser) authenticationToken.getPrincipal();
            } else if (authentication instanceof PreAuthenticatedAuthenticationToken) {
                // 刷新token方式
                PreAuthenticatedAuthenticationToken authenticationToken = (PreAuthenticatedAuthenticationToken) authentication;
                return (LoginAppUser) authenticationToken.getPrincipal();

            }
        }

        // 当内部服务，不带token时，内部服务
        String accessToken = TokenUtil.getToken();
        RedisTemplate redisTemplate = SpringUtils.getBean(RedisTemplate.class);
        Map<String, Object> params = (Map<String, Object>) redisTemplate.opsForValue()
                .get(UaaConstant.TOKEN + ":" + accessToken);
        if (params != null) {
            return (LoginAppUser) params.get(UaaConstant.AUTH);
        }

        return null;
    }
}
