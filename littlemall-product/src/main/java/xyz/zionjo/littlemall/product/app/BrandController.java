package xyz.zionjo.littlemall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import xyz.zionjo.common.valid.AddGroup;
import xyz.zionjo.common.valid.UpdateGroup;
import xyz.zionjo.common.valid.UpdateStatusGroup;
import xyz.zionjo.littlemall.product.entity.BrandEntity;
import xyz.zionjo.littlemall.product.service.BrandService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.R;


/**
 * 品牌
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 10:57:14
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 信息
     */
    @GetMapping("/infos")
    //@RequiresPermissions("product:brand:info")
    public R infos(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brands = brandService.getByIds(brandIds);

        return R.ok().put("brands", brands);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand/*, BindingResult result*/){
		/*if(result.hasErrors()){
            HashMap<String, String> map = new HashMap<>();

            result.getFieldErrors().forEach(item -> {
                String defaultMessage = item.getDefaultMessage();
                String field = item.getField();
                map.put(field,defaultMessage);

            });
		    return R.error(400,"提交的数据不合法").put("data",map);
        }else{

        }*/
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
