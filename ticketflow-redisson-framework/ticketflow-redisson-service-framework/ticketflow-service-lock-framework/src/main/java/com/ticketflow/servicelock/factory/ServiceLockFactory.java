package com.ticketflow.servicelock.factory;

import com.ticketflow.core.ManageLocker;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.ServiceLocker;
import lombok.AllArgsConstructor;


/**
 * @Description: 分布式锁类型工厂
 * @Author: rickey-c
 * @Date: 2025/1/27 16:21
 */
@AllArgsConstructor
public class ServiceLockFactory {

    private final ManageLocker manageLocker;

    public ServiceLocker getLock(LockType lockType) {
        return switch (lockType) {
            case Fair -> manageLocker.getFairLocker();
            case Write -> manageLocker.getWriteLocker();
            case Read -> manageLocker.getReadLocker();
            default -> manageLocker.getReentrantLocker();
        };
    }

}
