package com.ticketflow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 字符串工具类
 * @Author: rickey-c
 * @Date: 2025/1/23 16:41
 */
public class StringUtil {
    private final static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * 判断字符串不为空
     *
     * @param str 字符串
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return (str != null && !str.isEmpty() && !str.trim().isEmpty() && !"null".equalsIgnoreCase(str.trim())
                && !"undefined".equalsIgnoreCase(str.trim()) && !"NULL".equalsIgnoreCase(str.trim()));

    }

    /**
     * 判断字符串为空
     *
     * @param str 字符串
     * @return
     */
    public static boolean isEmpty(String str) {
        return !StringUtil.isNotEmpty(str);
    }

    /**
     * 将流转换为字符串
     *
     * @param is 文件流
     * @return
     */
    public static String inputStreamConvertString(InputStream is) {
        ByteArrayOutputStream baos = null;
        String result = null;
        try {
            if (is != null) {
                baos = new ByteArrayOutputStream();
                int i;
                while ((i = is.read()) != -1) {
                    baos.write(i);
                }
                result = baos.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("流转换为字符串失败！");
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    logger.error("关闭流失败！");
                }
            }
        }
        return result;
    }

    /**
     * 将URL参数转成map
     */
    public static Map<String, String> convertQueryStringToMap(String queryString) {
        Map<String, String> resultMap = new HashMap<>(256);
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                try {
                    String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    resultMap.put(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultMap;
    }
}
