package com.zss.microservices.log.service;

import com.zss.microservices.common.model.SysLog;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/10 19:39
 */
public interface LogService {
    void save(SysLog log);
}
