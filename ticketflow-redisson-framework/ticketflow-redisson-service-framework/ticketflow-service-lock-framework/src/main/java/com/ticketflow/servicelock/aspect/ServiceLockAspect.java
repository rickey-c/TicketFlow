package com.ticketflow.servicelock.aspect;


import com.ticketflow.constant.LockInfoType;
import com.ticketflow.lockinfo.LockInfoHandle;
import com.ticketflow.lockinfo.factory.LockInfoHandleFactory;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.ServiceLocker;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.servicelock.factory.ServiceLockFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 分布式锁
 * @Author: rickey-c
 * @Date: 2025/1/27 16:28
 */
@Slf4j
@Aspect
@Order(-10)
@AllArgsConstructor
public class ServiceLockAspect {

    private final LockInfoHandleFactory lockInfoHandleFactory;

    private final ServiceLockFactory serviceLockFactory;

    /**
     * 执行环切AOP逻辑
     *
     * @param joinPoint 切点
     * @param serviceLock 参数，分布式锁
     * @return 执行方法
     */
    @Around(value = "@annotation(serviceLock)")
    public Object around(ProceedingJoinPoint joinPoint, ServiceLock serviceLock) throws Throwable {
        // 分布式锁处理器
        LockInfoHandle lockInfoHandler = lockInfoHandleFactory.getLockInfoHandler(LockInfoType.SERVICE_LOCK);
        // 拼接锁名称
        String lockName = lockInfoHandler.getLockName(joinPoint, serviceLock.name(), serviceLock.keys());
        // 锁类型
        LockType lockType = serviceLock.lockType();
        // 锁等待时间
        long waitTime = serviceLock.waitTime();
        // 时间单位
        TimeUnit timeUnit = serviceLock.timeUnit();

        ServiceLocker lock = serviceLockFactory.getLock(lockType);

        boolean lockSuccess = lock.tryLock(lockName, timeUnit, waitTime);

        if (lockSuccess) {
            try {
                return joinPoint.proceed();
            } finally {
                lock.unlock(lockName);
            }

        } else {
            log.warn("Timeout while acquiring serviceLock:{}", lockName);
            // 降级逻辑
            String customLockTimeOutStrategy = serviceLock.customLockTimeOutStrategy();
            if (StringUtils.isNotEmpty(customLockTimeOutStrategy)) {
                return handleCustomLockTimeoutStrategy(customLockTimeOutStrategy, joinPoint);
            } else {
                serviceLock.lockTimeOutStrategy().handler(lockName);
            }
            return joinPoint.proceed();
        }
    }

    /**
     * 执行自定义失败方法
     *
     * @param customLockTimeoutStrategy 锁超时策略
     * @param joinPoint 切点
     * @return 方法执行结果
     */
    public Object handleCustomLockTimeoutStrategy(String customLockTimeoutStrategy, JoinPoint joinPoint) {
        // 获取当前方法的信息，包括方法名和参数类型
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取被代理的目标对象
        Object target = joinPoint.getTarget();
        // 用于保存用户指定的超时处理方法
        Method handleMethod = getMethod(customLockTimeoutStrategy, target, currentMethod);

        // 获取当前方法的参数，用于在后续调用目标方法时传递
        Object[] args = joinPoint.getArgs();

        // 用于存储目标方法的执行结果
        Object result;

        try {
            // 使用反射调用目标方法，传递当前方法的参数
            result = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            // 如果目标方法无法访问（不合法访问），抛出运行时异常
            throw new RuntimeException("Fail to illegal access custom lock timeout handler: " + customLockTimeoutStrategy, e);
        } catch (InvocationTargetException e) {
            // 如果目标方法在执行过程中抛出异常，抛出运行时异常
            throw new RuntimeException("Fail to invoke custom lock timeout handler: " + customLockTimeoutStrategy, e);
        }

        // 返回目标方法的执行结果
        return result;
    }

    /**
     * 获取用户的超时处理方法
     *
     * @param customLockTimeoutStrategy 用户的锁超时方法
     * @param target 被代理的目标对象
     * @param currentMethod 当前的方法
     * @return 超时处理方法
     */
    private static Method getMethod(String customLockTimeoutStrategy, Object target, Method currentMethod) {
        Method handleMethod ;
        try {
            // 根据用户指定的方法名和当前方法的参数类型，在目标类中查找对应的方法
            handleMethod = target.getClass().getDeclaredMethod(customLockTimeoutStrategy, currentMethod.getParameterTypes());
            // 设置访问权限为可访问，即使方法是私有的
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // 如果目标类中找不到指定的方法，抛出运行时异常，并提示错误信息
            throw new RuntimeException("Illegal annotation param customLockTimeoutStrategy :" + customLockTimeoutStrategy, e);
        }
        return handleMethod;
    }


}



