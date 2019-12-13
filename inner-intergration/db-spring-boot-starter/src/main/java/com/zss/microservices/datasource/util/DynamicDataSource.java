package com.zss.microservices.datasource.util;

import com.zss.microservices.datasource.constant.DataSourceKey;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/6 9:49
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private Map<Object, Object> datasource;

    public DynamicDataSource(){
        datasource = new HashMap();
        super.setTargetDataSources(datasource);
    }

    public <T extends DataSource> void addDataSource(DataSourceKey key, T dataSource){
        datasource.put(key, dataSource);
    }
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceHolder.getDataSourceKey();
    }
}
