package xyz.zionjo.littlemall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class LittlemallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittlemallGatewayApplication.class, args);
    }

}
