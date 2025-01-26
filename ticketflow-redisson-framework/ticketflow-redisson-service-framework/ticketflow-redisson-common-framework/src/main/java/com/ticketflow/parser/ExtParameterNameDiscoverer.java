package com.ticketflow.parser;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.NativeDetector;

/**
 * @Description: 对DefaultParameterNameDiscoverer进行扩展
 * @Author: rickey-c
 * @Date: 2025/1/26 14:50
 */
public class ExtParameterNameDiscoverer extends DefaultParameterNameDiscoverer {
    
    public ExtParameterNameDiscoverer() {
        super();
        if (!NativeDetector.inNativeImage()) {
            addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
        }
    }
}