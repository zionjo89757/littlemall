package xyz.zionjo.littlemall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import xyz.zionjo.littlemall.coupon.entity.SkuLadderEntity;
import xyz.zionjo.littlemall.coupon.service.SkuLadderService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.R;



/**
 * 商品阶梯价格
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 11:27:02
 */
@RestController
@RequestMapping("coupon/skuladder")
public class SkuLadderController {
    @Autowired
    private SkuLadderService skuLadderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:skuladder:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuLadderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:skuladder:info")
    public R info(@PathVariable("id") Long id){
		SkuLadderEntity skuLadder = skuLadderService.getById(id);

        return R.ok().put("skuLadder", skuLadder);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:skuladder:save")
    public R save(@RequestBody SkuLadderEntity skuLadder){
		skuLadderService.save(skuLadder);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:skuladder:update")
    public R update(@RequestBody SkuLadderEntity skuLadder){
		skuLadderService.updateById(skuLadder);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:skuladder:delete")
    public R delete(@RequestBody Long[] ids){
		skuLadderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
