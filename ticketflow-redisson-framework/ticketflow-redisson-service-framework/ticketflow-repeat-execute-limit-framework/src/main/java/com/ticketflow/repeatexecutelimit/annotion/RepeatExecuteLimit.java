package com.ticketflow.repeatexecutelimit.annotion;

import lombok.AllArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 幂等注解
 * @Author: rickey-c
 * @Date: 2025/1/27 20:27
 */
@Target(value = {ElementType.TYPE,ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RepeatExecuteLimit {

    /**
     * 幂等锁名称
     * @return name
     */
    String name() default "";

    /**
     * key设置
     * @return key
     */
    String[] keys();

    /**
     * 幂等保持时间，默认是业务执行时间
     * @return durationTime
     */
    long durationTime() default 0L;

    /**
     * 错误信息
     * @return message
     */
    String message() default "提交频繁，请稍后重试";
}
