package com.zss.microservices.uaa.server;

import com.zss.microservices.common.feign.FeignInterceptorConfig;
import com.zss.microservices.common.rest.RestTemplateConfig;
import com.zss.microservices.uaa.server.service.RedisAuthorizationCodeServices;
import com.zss.microservices.uaa.server.service.RedisClientDetailsService;
import com.zss.microservices.uaa.server.token.RedisTemplateTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.sql.DataSource;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/12 19:21
 * 认证服务器
 */
@Configuration
@Import({RestTemplateConfig.class,FeignInterceptorConfig.class})
public class UAAServerConfig {

    @Bean
    public RedisClientDetailsService redisClientDetailsService(DataSource dataSource,RedisTemplate<String, Object> redisTemplate){
        RedisClientDetailsService redisClientDetailsService = new RedisClientDetailsService(dataSource);
        redisClientDetailsService.setRedisTemplate(redisTemplate);
        return redisClientDetailsService;
    }

    @Bean
    public RandomValueAuthorizationCodeServices authorizationCodeServices(RedisTemplate<String,Object> redisTemplate){
        RedisAuthorizationCodeServices redisAuthorizationCodeServices = new RedisAuthorizationCodeServices();
        redisAuthorizationCodeServices.setRedisTemplate(redisTemplate);
        return redisAuthorizationCodeServices;
    }

    @Configuration
    @EnableAuthorizationServer
    public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private UserDetailsService userDetailsService;

        @Autowired(required = false)
        private RedisTemplateTokenStore redisTemplateTokenStore;

        @Autowired(required = false)
        private JwtTokenStore jwtTokenStore;

        @Autowired(required = false)
        private JwtAccessTokenConverter jwtAccessTokenConverter;

        @Autowired
        private WebResponseExceptionTranslator webResponseExceptionTranslator;

        @Autowired(required = false)
        private RandomValueAuthorizationCodeServices authorizationCodeServices;

        @Autowired
        private RedisClientDetailsService redisClientDetailsService;

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            super.configure(security);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(redisClientDetailsService);
            redisClientDetailsService.loadAllClientToCache();
        }

        /**
         * 配置身份认证器，配置认证方式，TokenStore，TokenGranter，OAuth2RequestFactory
         */
        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            if(jwtTokenStore != null){
                endpoints.tokenStore(jwtTokenStore)
                        .authenticationManager(authenticationManager)
                        .userDetailsService(userDetailsService);
            }else if(redisTemplateTokenStore != null){
                endpoints.tokenStore(redisTemplateTokenStore)
                        .authenticationManager(authenticationManager)
                        .userDetailsService(userDetailsService);
            }
            if(jwtAccessTokenConverter != null){
                endpoints.accessTokenConverter(jwtAccessTokenConverter);
            }
            endpoints.authorizationCodeServices(authorizationCodeServices);
            endpoints.exceptionTranslator(webResponseExceptionTranslator);
        }
    }

}
