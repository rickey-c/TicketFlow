package com.ticketflow.lockinfo.impl;

import com.ticketflow.lockinfo.AbstractLockInfoHandle;

/**
 * @Description: 锁信息实现（分布式锁）
 * @Author: rickey-c
 * @Date: 2025/1/27 17:11
 */
public class ServiceLockInfoHandle extends AbstractLockInfoHandle {

    @Override
    protected String getLockPrefixName() {
        return "service_lock";
    }
}
