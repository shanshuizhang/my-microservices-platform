package com.zss.microservices.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.zss.microservices.datasource.constant.DataSourceKey;
import com.zss.microservices.datasource.util.DynamicDataSource;
import com.zss.microservices.datasource.util.ModuloDatabaseShardingAlgorithm;
import com.zss.microservices.datasource.util.ModuloTableShardingAlgorithm;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/9 16:41
 */
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ConditionalOnProperty(name = {"spring.datasource.sharding.enable"},matchIfMissing = false,havingValue = "true")
public class ShardingDataSourceConfig {


    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource dataSourceMaster(){
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave")
    public DataSource dataSourceSlave(){
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.single")
    public DataSource dataSourceSingle(){
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "shardingDataSource")
    public DataSource getShardingDataSource(@Qualifier("dataSourceMaster")DataSource dataSourceMaster, @Qualifier("dataSourceSlave")DataSource dataSourceSlave) throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getUserTableRuleConfiguration());
        shardingRuleConfig.getBindingTableGroups().add("t_order");
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id",ModuloDatabaseShardingAlgorithm.class.getName()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("order_id",ModuloTableShardingAlgorithm.class.getName()));
        Map<String, DataSource> dataSourceMap = new HashMap(4);
        dataSourceMap.put("test_msg0", dataSourceMaster);
        dataSourceMap.put("test_msg1", dataSourceSlave);
        return new ShardingDataSource(shardingRuleConfig.build(dataSourceMap));
    }

    @Bean
    public TableRuleConfiguration getUserTableRuleConfiguration(){
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        //配置逻辑表名，并非数据库中真实存在的表名，而是sql中使用的那个，不受分片策略而改变.
        //例如：select * frpm t_order where user_id = xxx
        orderTableRuleConfig.setLogicTable("t_order");
        //配置真实的数据节点，即数据库中真实存在的节点，由数据源名 + 表名组成
        //${} 是一个groovy表达式，[]表示枚举，{...}表示一个范围。
        //整个inline表达式最终会是一个笛卡尔积，表示ds_0.t_order_0. ds_0.t_order_1
        // ds_1.t_order_0. ds_1.t_order_0
        orderTableRuleConfig.setActualDataNodes("test_msg${0..1}.t_order_${0..1}");
        //主键生成列，默认的主键生成算法是snowflake
        orderTableRuleConfig.setKeyGeneratorColumnName("order_id");
        return orderTableRuleConfig;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("shardingDataSource") DataSource shardingDataSource){
        DynamicDataSource dataSource = new DynamicDataSource();

        dataSource.addDataSource(DataSourceKey.core,dataSourceSingle());
        dataSource.addDataSource(DataSourceKey.sharding,shardingDataSource);
        dataSource.setDefaultTargetDataSource(dataSourceSingle());

        return dataSource;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception{
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:com/central/**/dao/*.xml"));
        return sessionFactoryBean.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }
}
