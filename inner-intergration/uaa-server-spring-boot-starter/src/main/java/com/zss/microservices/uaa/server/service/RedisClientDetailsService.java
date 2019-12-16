package com.zss.microservices.uaa.server.service;

import com.alibaba.fastjson.JSONObject;
import com.zss.microservices.common.auth.details.DefaultClientDetails;
import com.zss.microservices.common.constant.UaaConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/16 16:20
 */
@Slf4j
public class RedisClientDetailsService extends JdbcClientDetailsService{

    // 扩展 默认的 ClientDetailsService, 增加逻辑删除判断( status = 1)
    private static final String SELECT_CLIENT_DETAILS_SQL = "select client_id, client_secret, resource_ids, scope, authorized_grant_types, " +
            "web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove ,if_limit, limit_count " +
            "from oauth_client_details where client_id = ? and `status` = 1 ";


    private static final String SELECT_FIND_STATEMENT = "select client_id, client_secret,resource_ids, scope, "
            + "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
            + "refresh_token_validity, additional_information, autoapprove ,if_limit, limit_count    from oauth_client_details where `status` = 1 order by client_id " ;

    private final JdbcTemplate jdbcTemplate;

    public RedisClientDetailsService(DataSource dataSource){
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        setSelectClientDetailsSql(SELECT_CLIENT_DETAILS_SQL) ;
        setFindClientDetailsSql(SELECT_FIND_STATEMENT) ;
    }

    private RedisTemplate<String, Object> redisTemplate;

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws InvalidClientException {
        ClientDetails clientDetails = null;
        String value = (String)redisTemplate.opsForHash().get(UaaConstant.CACHE_CLIENT_KEY,clientId);
        //String value = (String)redisTemplate.boundHashOps(UaaConstant.CACHE_CLIENT_KEY).get(clientId);
        if(StringUtils.isBlank(value)){
            clientDetails = cacheAndGetClient(clientId);
        }else{
            clientDetails = JSONObject.parseObject(value,BaseClientDetails.class);
        }
        return clientDetails;
    }

    /**
     * 从数据库获取ClientDetails返回，并缓存到redis中
     * @param clientId
     * @return
     */
    private ClientDetails cacheAndGetClient(String clientId){
        ClientDetails clientDetails = null;
        try{
            try{
                clientDetails = jdbcTemplate.queryForObject(SELECT_CLIENT_DETAILS_SQL,new ClientDetailsRowMapper(),clientId);
            }catch(EmptyResultDataAccessException e){
                throw new NoSuchClientException("No client with requested id: " + clientId);
            }
            if(clientDetails != null){
                redisTemplate.boundHashOps(UaaConstant.CACHE_CLIENT_KEY).put(clientId,JSONObject.toJSONString(clientDetails));
                log.info("缓存clientId:{},{}", clientId, clientDetails);
            }
        }catch(NoSuchClientException e){
            log.error("clientId:{},{}", clientId, e.getMessage());
            throw new AuthenticationException("应用不存在"){};
        }catch(InvalidClientException e){
            throw new AuthenticationException("应用状态不合法"){};
        }
        return clientDetails;
    }

    @Override
    public void updateClientDetails(ClientDetails clientDetails) throws NoSuchClientException {
        super.updateClientDetails(clientDetails);
        cacheAndGetClient(clientDetails.getClientId());
    }

    @Override
    public void updateClientSecret(String clientId, String secret) throws NoSuchClientException {
        super.updateClientSecret(clientId, secret);
        cacheAndGetClient(clientId);
    }

    @Override
    public void removeClientDetails(String clientId) throws NoSuchClientException {
        super.removeClientDetails(clientId);
        removeRedisCache(clientId);
    }

    /**
     * 删除redis缓存
     * @param clientId
     */
    private void removeRedisCache(String clientId){
        redisTemplate.boundHashOps(UaaConstant.CACHE_CLIENT_KEY).delete(clientId);
    }

    /**
     * 将oauth_client_details全表刷入redis
     */
    public void loadAllClientToCache(){
        if(redisTemplate.hasKey(UaaConstant.CACHE_CLIENT_KEY)){
            return;
        }
        log.info("将oauth_client_details全表刷入redis");
        List<ClientDetails> clientDetails = this.listClientDetails();
        if(CollectionUtils.isEmpty(clientDetails)){
            log.error("oauth_client_details表数据为空，请检查");
            return;
        }
        clientDetails.parallelStream().forEach(client -> {
            redisTemplate.boundHashOps(UaaConstant.CACHE_CLIENT_KEY).put(client.getClientId(),JSONObject.toJSONString(client));
        });
    }

    @Override
    public List<ClientDetails> listClientDetails(){
        return jdbcTemplate.query(SELECT_FIND_STATEMENT,new ClientDetailsRowMapper());
    }

    public static class ClientDetailsRowMapper implements RowMapper<ClientDetails>{

        private com.zss.microservices.uaa.server.json.JsonMapper mapper = createJsonMapper();

        @Override
        public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
            DefaultClientDetails details = new DefaultClientDetails(rs.getString(1), rs.getString(3), rs.getString(4),
                    rs.getString(5), rs.getString(7), rs.getString(6));
            details.setClientSecret(rs.getString(2));
            if (rs.getObject(8) != null) {
                details.setAccessTokenValiditySeconds(rs.getInt(8));
            }
            if (rs.getObject(9) != null) {
                details.setRefreshTokenValiditySeconds(rs.getInt(9));
            }
            String json = rs.getString(10);
            if (json != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> additionalInformation = mapper.read(json, Map.class);
                    details.setAdditionalInformation(additionalInformation);
                }
                catch (Exception e) {
                    log.warn("Could not decode JSON for additional information: " + details, e);
                }
            }
            String scopes = rs.getString(11);

            long ifLimit = rs.getLong(12) ;
            details.setIf_limit(ifLimit);

            long limitCount = rs.getLong(13) ;
            details.setLimit_count(limitCount);
            if (scopes != null) {
                details.setAutoApproveScopes(org.springframework.util.StringUtils.commaDelimitedListToSet(scopes));
            }
            return details;
        }
    }

    /**
     * json process
     * @return
     */
    private static com.zss.microservices.uaa.server.json.JsonMapper createJsonMapper() {
        if (ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper", null)) {
            return new com.zss.microservices.uaa.server.json.JacksonMapper();
        } else if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", null)) {
            return new com.zss.microservices.uaa.server.json.Jackson2Mapper();
        }
        return new com.zss.microservices.uaa.server.json.NotSupportedJsonMapper();
    }
}
