package com.ticketflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan({"com.ticketflow.mapper"})
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class ProgramApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgramApplication.class, args);
    }

}
