package xyz.zionjo.littlemall.member.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.zionjo.common.utils.R;

@FeignClient("littlemall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/member/list")
    public R member2coupon();
}
