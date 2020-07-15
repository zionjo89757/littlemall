package xyz.zionjo.littlemall.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.zionjo.common.utils.R;

@FeignClient("littlemall-third-party")
public interface ThirdPartyFeignService {
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
