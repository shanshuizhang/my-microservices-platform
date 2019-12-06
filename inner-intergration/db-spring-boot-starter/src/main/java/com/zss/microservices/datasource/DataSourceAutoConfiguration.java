package com.zss.microservices.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/5 19:33
 */
@Configuration
public class DataSourceAutoConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.druid.core")
    public DataSource dataSourceCore(){
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.core.log")
    public DataSource dataSourceLog(){
        return DruidDataSourceBuilder.create().build();
    }
}
