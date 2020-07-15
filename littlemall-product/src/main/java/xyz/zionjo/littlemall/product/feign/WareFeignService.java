package xyz.zionjo.littlemall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.zionjo.common.to.SkuHasStockVo;
import xyz.zionjo.common.utils.R;

import java.util.List;

@FeignClient("littlemall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
