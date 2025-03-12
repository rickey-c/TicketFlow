package com.ticketflow.base;

import com.ticketflow.threadlocal.BaseParameterHolder;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Description: 线程池基类
 * @Author: rickey-c
 * @Date: 2025/1/23 15:31
 */
public class BaseThreadPool {

    /**
     * 获取当前的MDC上下文Map
     *
     * @return 当前的MDC上下文Map
     */
    protected static Map<String, String> getContextForTask() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 获取当前的ThreadLocal的参数Map
     *
     * @return 当前的ThreadLocal的参数Map
     */
    protected static Map<String, String> getContextForHold() {
        return BaseParameterHolder.getParameterMap();
    }

    /**
     * Runnable包装线程池异步线程
     *
     * @param runnable runnable任务
     * @param parentMDCContext 父线程的MDC上下文
     * @param parentHoldContext 父线程的ThreadLocal上下文
     * @return 包装之后的任务
     */
    protected static Runnable wrapTask(Runnable runnable,
                                       final Map<String, String> parentMDCContext,
                                       final Map<String, String> parentHoldContext) {
        return () -> {
            Map<String, Map<String, String>> preprocess = preprocess(parentMDCContext, parentHoldContext);
            // 暂存当前线程的context
            Map<String, String> holdContext = preprocess.get("holdContext");
            Map<String, String> mdcContext = preprocess.get("mdcContext");
            try {
                runnable.run();
            } finally {
                postProcess(holdContext, mdcContext);
            }
        };
    }


    /**
     * Callable包装线程池异步线程
     *
     * @param task 任务
     * @param parentMdcContext 父线程的MDC上下文
     * @param parentHoldContext 父线程的ThreadLocal上下文
     * @param <T> 泛型
     * @return 包装之后的Callable
     */
    protected static <T> Callable<T> wrapTask(Callable<T> task,
                                              final Map<String, String> parentMdcContext,
                                              final Map<String, String> parentHoldContext) {
        return () -> {
            Map<String, Map<String, String>> preprocess = preprocess(parentMdcContext, parentHoldContext);
            Map<String, String> holdContext = preprocess.get("holdContext");
            Map<String, String> mdcContext = preprocess.get("mdcContext");
            try {
                return task.call();
            } finally {
                postProcess(mdcContext, holdContext);
            }
        };
    }

    /**
     * 设置父线程的MDC和HolderContext到线程池的子线程中
     *
     * @param parentMDCContext 父线程的MDC上下文
     * @param parentHoldContext 父线程的ThreadLocal上下文
     * @return 包装之后的map
     */
    protected static Map<String, Map<String, String>> preprocess(Map<String, String> parentMDCContext, Map<String, String> parentHoldContext) {
        Map<String, Map<String, String>> map = new HashMap<>(8);
        // 暂存当前线程的context
        Map<String, String> holdContext = BaseParameterHolder.getParameterMap();
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        if (parentMDCContext == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(parentMDCContext);
        }
        if (parentHoldContext == null) {
            BaseParameterHolder.removeParameterMap();
        } else {
            BaseParameterHolder.setParameterMap(parentHoldContext);
        }
        map.put("holdContext", holdContext);
        map.put("mdcContext", mdcContext);
        return map;
    }

    /**
     * 执行完之后替换回来
     *
     * @param mdcContext 原本的mdc上下文
     * @param holdContext 原本的ThreadLocal
     */
    private static void postProcess(Map<String, String> mdcContext, Map<String, String> holdContext) {
        if (mdcContext == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(mdcContext);
        }
        if (holdContext == null) {
            BaseParameterHolder.removeParameterMap();
        } else {
            BaseParameterHolder.setParameterMap(holdContext);
        }
    }


}
