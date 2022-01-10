package com.roger.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * gateway application
 * @author roger
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.roger.**.utils", "com.roger.common.security.config", "com.roger.gateway"})
public class EladminGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EladminGatewayApplication.class, args);
    }

}
