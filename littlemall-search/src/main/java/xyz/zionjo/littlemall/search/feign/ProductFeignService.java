package xyz.zionjo.littlemall.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import xyz.zionjo.common.utils.R;

import java.util.List;

@FeignClient("littlemall-product")
public interface ProductFeignService {
    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    public R brandInfos(@RequestParam("brandIds") List<Long> brandIds);
}
