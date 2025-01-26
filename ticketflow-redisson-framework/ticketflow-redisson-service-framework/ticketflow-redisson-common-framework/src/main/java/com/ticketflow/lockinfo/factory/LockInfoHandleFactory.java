package com.ticketflow.lockinfo.factory;

import com.ticketflow.lockinfo.LockInfoHandle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Description: 锁信息工厂
 * @Author: rickey-c
 * @Date: 2025/1/26 14:48
 */
public class LockInfoHandleFactory implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    public LockInfoHandle getLockInfoHandler(String lockInfoType){
        return applicationContext.getBean(lockInfoType, LockInfoHandle.class);
    }
    

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
