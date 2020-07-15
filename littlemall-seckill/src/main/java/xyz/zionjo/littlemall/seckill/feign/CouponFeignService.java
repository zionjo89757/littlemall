package xyz.zionjo.littlemall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.zionjo.common.utils.R;

@FeignClient("littlemall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();
}
