package xyz.zionjo.littlemall.product.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.zionjo.littlemall.product.service.SkuInfoService;
import xyz.zionjo.littlemall.product.vo.SkuItemVo;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {
    @Autowired

    SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemVo);
        return "item";
    }
}
