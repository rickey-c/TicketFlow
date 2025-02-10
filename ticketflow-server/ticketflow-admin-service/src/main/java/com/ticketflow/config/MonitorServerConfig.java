package com.ticketflow.config;


import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import com.ticketflow.monitor.DingTalkMessage;
import com.ticketflow.monitor.MonitorServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 服务监控配置
 * @Author: rickey-c
 * @Date: 2025/1/24 14:33
 */
@Configuration
public class MonitorServerConfig {

    @Value("${dingtalk.token:}")
    private String token;

    @Bean
    public DingTalkMessage dingTalkMessage() {
        return new DingTalkMessage(token);
    }

    @Bean
    public MonitorServer monitorServer(DingTalkMessage dingTalkMessage, InstanceRepository repository) {
        return new MonitorServer(dingTalkMessage, repository);
    }


}
