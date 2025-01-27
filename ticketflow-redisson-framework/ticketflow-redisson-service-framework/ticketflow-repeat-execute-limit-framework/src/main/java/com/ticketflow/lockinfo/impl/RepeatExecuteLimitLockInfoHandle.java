package com.ticketflow.lockinfo.impl;

import com.ticketflow.lockinfo.AbstractLockInfoHandle;

/**
 * @Description: 锁信息实现（幂等锁）
 * @Author: rickey-c
 * @Date: 2025/1/27 20:21
 */
public class RepeatExecuteLimitLockInfoHandle extends AbstractLockInfoHandle {

    private static final String PREFIX_NAME = "repeat_execute_limit";
    
    @Override
    protected String getLockPrefixName() {
        return PREFIX_NAME; 
    }
}
