package xyz.zionjo.littlemall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "xyz.zionjo.littlemall.product.feign")
@SpringBootApplication
public class LittlemallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittlemallProductApplication.class, args);
    }

}
