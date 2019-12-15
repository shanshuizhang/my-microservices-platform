package com.zss.microservices.uaa.server.token;

import com.zss.microservices.common.auth.details.LoginAppUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis集群存储token
 */
public class RedisTemplateTokenStore implements TokenStore {

    private static final String ACCESS = "access:";
    private static final String AUTH_TO_ACCESS = "auth_to_access:";
    private static final String AUTH = "auth:";
    private static final String REFRESH_AUTH = "refresh_auth:";
    private static final String ACCESS_TO_REFRESH = "access_to_refresh:";
    private static final String REFRESH = "refresh:";
    private static final String REFRESH_TO_ACCESS = "refresh_to_access:";
    private static final String CLIENT_ID_TO_ACCESS = "client_id_to_access:";
    private static final String UNAME_TO_ACCESS = "uname_to_access:";
    private static final String TOKEN = "token:";

    private RedisTemplate<String,Object> redisTemplate;

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    public OAuth2AccessToken getAccessToken(OAuth2Authentication oAuth2Authentication) {
        String key = authenticationKeyGenerator.extractKey(oAuth2Authentication);
        OAuth2AccessToken accessToken = (OAuth2AccessToken)redisTemplate.opsForValue().get(AUTH_TO_ACCESS + key);
        if(accessToken != null
                && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))){
            // Keep the stores consistent (maybe the same user is represented by
            // this authentication but the details
            // have changed)
            storeAccessToken(accessToken,oAuth2Authentication);
        }
        return accessToken;
    }

    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    public OAuth2Authentication readAuthentication(String token) {
        return (OAuth2Authentication)redisTemplate.opsForValue().get(AUTH + token);
    }

    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        redisTemplate.opsForValue().set(ACCESS + token.getValue(),token);
        redisTemplate.opsForValue().set(AUTH + token.getValue(),authentication);
        redisTemplate.opsForValue().set(AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(authentication),token);

        Map<String,Object> params = new HashMap<>();
        params.put("clientId",authentication.getOAuth2Request().getClientId());

        if(authentication.getUserAuthentication() instanceof UsernamePasswordAuthenticationToken){
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken)authentication.getUserAuthentication();
            LoginAppUser appUser = (LoginAppUser)authenticationToken.getPrincipal();
            params.put("username", appUser.getUsername());
            params.put("auth",  appUser );
            params.put("authorities", appUser.getAuthorities());
        }

        if(!params.isEmpty()){
            redisTemplate.opsForValue().set(TOKEN + token.getValue(),params);
        }

        if(!authentication.isClientOnly()){
            if(token != null){
                if(token.getExpiration() != null){
                    int seconds = token.getExpiresIn();
                    redisTemplate.expire(UNAME_TO_ACCESS + authentication.getOAuth2Request().getClientId(), seconds,
                            TimeUnit.SECONDS);
                }else {
                    redisTemplate.opsForList().rightPush(UNAME_TO_ACCESS + getApprovalKey(authentication),token);
                }
            }else {
                redisTemplate.opsForList().rightPush(UNAME_TO_ACCESS + getApprovalKey(authentication),token);
            }
        }

        if (token != null) {
            if (token.getExpiration() != null) {
                int seconds = token.getExpiresIn();
                redisTemplate.expire(CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId(), seconds,
                        TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForList()
                        .rightPush(CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId(), token);
            }
        } else {
            redisTemplate.opsForList().rightPush(CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId(),
                    token);
        }

        if (token.getExpiration() != null) {
            int seconds = token.getExpiresIn();
            redisTemplate.expire(ACCESS + token.getValue(), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(AUTH + token.getValue(), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(TOKEN + token.getValue(), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(authentication), seconds,
                    TimeUnit.SECONDS);
            redisTemplate.expire(CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId(), seconds,
                    TimeUnit.SECONDS);
            redisTemplate.expire(UNAME_TO_ACCESS + getApprovalKey(authentication), seconds, TimeUnit.SECONDS);
        }

        OAuth2RefreshToken refreshToken = token.getRefreshToken();

        if (token.getRefreshToken() != null && token.getRefreshToken().getValue() != null) {
            this.redisTemplate.opsForValue().set(REFRESH_TO_ACCESS + token.getRefreshToken().getValue(),
                    token.getValue());
            this.redisTemplate.opsForValue().set(ACCESS_TO_REFRESH + token.getValue(),
                    token.getRefreshToken().getValue());

            if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
                Date expiration = expiringRefreshToken.getExpiration();
                if (expiration != null) {
                    int seconds = Long.valueOf((expiration.getTime() - System.currentTimeMillis()) / 1000L).intValue();

                    redisTemplate.expire(REFRESH_TO_ACCESS + token.getRefreshToken().getValue(), seconds,
                            TimeUnit.SECONDS);
                    redisTemplate.expire(ACCESS_TO_REFRESH + token.getValue(), seconds, TimeUnit.SECONDS);

                }
            }

        }
    }

    private String getApprovalKey(OAuth2Authentication authentication){
        String userName = authentication.getUserAuthentication() == null ? ""
                : authentication.getUserAuthentication().getName();
        return getApprovalKey(authentication.getOAuth2Request().getClientId(),userName);
    }

    private String getApprovalKey(String clientId, String userName) {
        return clientId + (userName == null ? "" : ":" + userName);
    }

    public OAuth2AccessToken readAccessToken(String s) {
        return null;
    }

    public void removeAccessToken(OAuth2AccessToken oAuth2AccessToken) {

    }

    public void storeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken, OAuth2Authentication oAuth2Authentication) {

    }

    public OAuth2RefreshToken readRefreshToken(String s) {
        return null;
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        return null;
    }

    public void removeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {

    }

    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {

    }



    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String s, String s1) {
        return null;
    }

    public Collection<OAuth2AccessToken> findTokensByClientId(String s) {
        return null;
    }
}
