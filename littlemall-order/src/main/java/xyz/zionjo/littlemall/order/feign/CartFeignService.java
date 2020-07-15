package xyz.zionjo.littlemall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.zionjo.littlemall.order.vo.OrderItemVo;

import java.util.List;


@FeignClient("littlemall-cart")
public interface CartFeignService {


    @ResponseBody
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
