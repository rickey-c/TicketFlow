package com.ticketflow.lockinfo.impl;

import com.ticketflow.lockinfo.AbstractLockInfoHandle;

/**
 * @Description: 锁信息实现（分布式锁）
 * @Author: rickey-c
 * @Date: 2025/1/27 17:11
 */
public class ServiceLockInfoHandle extends AbstractLockInfoHandle {

    private static final String LOCK_PREFIX_NAME = "SERVICE_LOCK";

    @Override
    protected String getLockPrefixName() {
        return LOCK_PREFIX_NAME;
    }
}
