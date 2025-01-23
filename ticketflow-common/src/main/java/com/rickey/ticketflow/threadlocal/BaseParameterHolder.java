package com.rickey.ticketflow.threadlocal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Description: ThreadLocal的线程绑定工具
 * @Author: rickey-c
 * @Date: 2025/1/23 16:23
 */
public class BaseParameterHolder {

    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL_MAP = new ThreadLocal<>();

    public static void setParameter(String key, String value) {
        Map<String, String> map = THREAD_LOCAL_MAP.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(key, value);
        THREAD_LOCAL_MAP.set(map);
    }

    public static String getParameter(String key) {
        return Optional.ofNullable(THREAD_LOCAL_MAP.get()).map(map -> map.get(key)).orElse(null);
    }

    public static void removeParameter(String key) {
        Map<String, String> map = THREAD_LOCAL_MAP.get();
        if (map != null) {
            map.remove(key);
        }
    }

    public static ThreadLocal<Map<String, String>> getThreadLocal() {
        return THREAD_LOCAL_MAP;
    }

    public static Map<String, String> getParameterMap() {
        Map<String, String> map = THREAD_LOCAL_MAP.get();
        if (map == null) {
            map = new HashMap<>(64);
        }
        return map;
    }

    public static void setParameterMap(Map<String, String> map) {
        THREAD_LOCAL_MAP.set(map);
    }

    public static void removeParameterMap() {
        THREAD_LOCAL_MAP.remove();
    }
}
