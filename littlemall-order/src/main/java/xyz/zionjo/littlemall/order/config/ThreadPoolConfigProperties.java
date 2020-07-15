package xyz.zionjo.littlemall.order.config;

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
