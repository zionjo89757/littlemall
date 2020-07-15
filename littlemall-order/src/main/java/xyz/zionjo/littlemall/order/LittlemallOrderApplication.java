package xyz.zionjo.littlemall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@MapperScan("xyz.zionjo.littlemall.order.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class LittlemallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittlemallOrderApplication.class, args);
    }

}
