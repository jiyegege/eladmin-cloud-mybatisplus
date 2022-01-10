package com.roger.security;

import com.roger.common.core.utils.SpringContextHolder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * gateway application
 * @author roger
 */
@SpringBootApplication(scanBasePackages = {"com.roger", "com.roger.api.service"})
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
