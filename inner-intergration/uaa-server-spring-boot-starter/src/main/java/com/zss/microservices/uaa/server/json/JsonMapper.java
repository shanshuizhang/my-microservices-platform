package com.zss.microservices.uaa.server.json;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/16 18:17
 */
public interface JsonMapper {

    String write(Object input) throws Exception;

    <T> T read(String input,Class<T> type) throws Exception;
}
