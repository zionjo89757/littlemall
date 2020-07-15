package xyz.zionjo.littlemall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.zionjo.common.to.es.SkuEsModel;
import xyz.zionjo.common.utils.R;

import java.util.List;

@FeignClient("littlemall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
