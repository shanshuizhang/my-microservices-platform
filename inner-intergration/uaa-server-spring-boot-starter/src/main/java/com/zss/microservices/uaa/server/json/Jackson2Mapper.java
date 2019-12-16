package com.zss.microservices.uaa.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/16 18:25
 */
public class Jackson2Mapper implements JsonMapper {

    private ObjectMapper mapper = new ObjectMapper();
    @Override
    public String write(Object input) throws Exception {
        return mapper.writeValueAsString(input);
    }

    @Override
    public <T> T read(String input, Class<T> type) throws Exception {
        return mapper.readValue(input, type);
    }
}
