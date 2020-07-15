package xyz.zionjo.littlemall.product.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.zionjo.littlemall.product.entity.CategoryEntity;
import xyz.zionjo.littlemall.product.service.CategoryService;
import xyz.zionjo.littlemall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        // 查出所有一级分类

        List<CategoryEntity> categoryEntities = categoryService.getLevel1Category();

        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson(){
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }
}
