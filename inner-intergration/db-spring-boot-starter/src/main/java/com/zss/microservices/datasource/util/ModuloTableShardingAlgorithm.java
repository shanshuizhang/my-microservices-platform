package com.zss.microservices.datasource.util;

import io.shardingjdbc.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/9 17:52
 */
public class ModuloTableShardingAlgorithm implements PreciseShardingAlgorithm<Long> {
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        for(String each: collection){
            if(each.endsWith(Long.parseLong(preciseShardingValue.getValue().toString()) % 2 + "")){
                return each;
            }
        }
        throw new IllegalArgumentException();
    }
}
