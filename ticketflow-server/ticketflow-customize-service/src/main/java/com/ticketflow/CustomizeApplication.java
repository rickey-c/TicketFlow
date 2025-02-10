package com.ticketflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan({"com.ticketflow.mapper"})
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.ticketflow", "com.baidu.fsg.uid"})
public class CustomizeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomizeApplication.class, args);
    }

}
