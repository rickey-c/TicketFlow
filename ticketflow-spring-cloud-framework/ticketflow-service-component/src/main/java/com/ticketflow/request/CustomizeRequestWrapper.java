package com.ticketflow.request;

import com.ticketflow.utils.StringUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @Description: 自定义请求包装类
 * @Author: rickey-c
 * @Date: 2025/1/25 17:03
 */
public class CustomizeRequestWrapper extends HttpServletRequestWrapper {

    private final String requestBody;

    public CustomizeRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 获取requestBody
        requestBody = StringUtil.inputStreamConvertString(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        // 模拟请求，使得每次读取都能从头开始
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    /**
     * 包装使得请求体可以获取多次
     *
     * @return
     */
    public String getRequestBody() {
        return this.requestBody;
    }

}
