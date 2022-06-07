package com.roger.security;

import com.roger.common.core.utils.SpringContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * gateway application
 * @author roger
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.roger.*"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.roger.api.service")
public class EladminAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(EladminAuthApplication.class, args);
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

}
