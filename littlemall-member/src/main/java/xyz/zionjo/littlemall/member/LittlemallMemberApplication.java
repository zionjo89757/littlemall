package xyz.zionjo.littlemall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("xyz.zionjo.littlemall.member.dao")
@EnableFeignClients(basePackages = "xyz.zionjo.littlemall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class LittlemallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittlemallMemberApplication.class, args);
    }

}
