package com.ticketflow.servicelock.factory;

import com.ticketflow.core.ManageLocker;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.ServiceLocker;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @Description: 分布式锁类型工厂
 * @Author: rickey-c
 * @Date: 2025/1/27 16:21
 */
@AllArgsConstructor
public class ServiceLockFactory {

    private final ManageLocker manageLocker;

    public ServiceLocker getLock(LockType lockType) {
        ServiceLocker lock;
        switch (lockType) {
            case Fair:
                lock = manageLocker.getFairLocker();
                break;
            case Write:
                lock = manageLocker.getWriteLocker();
                break;
            case Read:
                lock = manageLocker.getReadLocker();
                break;
            default:
                lock = manageLocker.getReentrantLocker();
                break;
        }
        return lock;
    }

}
