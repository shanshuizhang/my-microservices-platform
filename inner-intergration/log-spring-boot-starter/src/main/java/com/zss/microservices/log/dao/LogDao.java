package com.zss.microservices.log.dao;

import com.zss.microservices.common.model.SysLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import javax.sql.DataSource;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 19:39
 * 保存日志
 * eureka-server配置不需要datasource,不会装配bean
 */
@Mapper
@ConditionalOnBean(DataSource.class)
public interface LogDao {

    @Insert("insert into sys_log(username, module, params, remark, flag, createTime) values(#{username}, #{module}, #{params}, #{remark}, #{flag}, #{createTime})")
    int save(SysLog sysLog);
}
