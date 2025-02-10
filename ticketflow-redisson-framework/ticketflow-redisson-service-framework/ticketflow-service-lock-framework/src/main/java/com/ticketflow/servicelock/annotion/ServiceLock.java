package com.ticketflow.servicelock.annotion;

import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.info.LockTimeOutStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 分布式锁注解
 * @Author: rickey-c
 * @Date: 2025/1/27 16:37
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ServiceLock {

    /**
     * 锁类型，默认可重入锁
     *
     * @return lockType
     */
    LockType lockType() default LockType.Reentrant;

    /**
     * 锁名称
     *
     * @return name
     */
    String name() default "";

    /**
     * 业务keys
     *
     * @return keys
     */
    String[] keys();

    /**
     * 锁等待时间
     *
     * @return waitTime
     */
    long waitTime() default 10;

    /**
     * 时间单位
     *
     * @return TimeUnit
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 加锁超时处理策略
     *
     * @return timeUnit
     */
    LockTimeOutStrategy lockTimeOutStrategy() default LockTimeOutStrategy.FAIL;

    /**
     * 自定义加锁超时处理策略
     *
     * @return customLockTimeOutStrategy
     */
    String customLockTimeOutStrategy() default "";

}
