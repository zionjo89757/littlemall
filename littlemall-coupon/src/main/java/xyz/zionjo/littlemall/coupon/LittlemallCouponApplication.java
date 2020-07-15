package xyz.zionjo.littlemall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("xyz.zionjo.littlemall.coupon.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class LittlemallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittlemallCouponApplication.class, args);
    }

}
