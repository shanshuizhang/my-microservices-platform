package com.zss.microservices.datasource.util;

import com.zss.microservices.datasource.constant.DataSourceKey;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/5 19:52
 */
public class DataSourceHolder {

    private static final ThreadLocal<DataSourceKey> dataSourceKey = new ThreadLocal<>();

    public static DataSourceKey getDataSourceKey() {
        return dataSourceKey.get();
    }

    public static void setDataSourceKey(DataSourceKey key) {
        dataSourceKey.set(key);
    }

    public static void clearDataSourceKey() {
        dataSourceKey.remove();
    }
}
