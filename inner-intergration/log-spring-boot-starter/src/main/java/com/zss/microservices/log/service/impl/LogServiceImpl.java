package com.zss.microservices.log.service.impl;

import com.zss.microservices.common.model.SysLog;
import com.zss.microservices.datasource.annotation.DataSource;
import com.zss.microservices.log.dao.LogDao;
import com.zss.microservices.log.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 19:52
 * 切换数据源，存储log-center
 */
public class LogServiceImpl implements LogService {

    @Autowired
    private LogDao logDao;

    @Async
    @Override
    @DataSource(name = "log")
    public void save(SysLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(new Date());
        }
        if (log.getFlag() == null) {
            log.setFlag(Boolean.TRUE);
        }
        logDao.save(log);
    }
}
