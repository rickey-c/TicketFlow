package com.ticketflow.servicelock.info;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/27 15:56
 */
public enum LockTimeOutStrategy implements LockTimeOutHandler{

    /**
     * 快速失败
     */
    FAIL(){
        @Override
        public void handler(String lockName) {
            String msg = String.format("%s请求频繁",lockName);
            throw new RuntimeException(msg);
        }
    }
}
