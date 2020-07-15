package xyz.zionjo.littlemall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


//@Component
@ConfigurationProperties(prefix = "littlemall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
