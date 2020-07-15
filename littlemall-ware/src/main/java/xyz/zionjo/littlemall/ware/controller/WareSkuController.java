package xyz.zionjo.littlemall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import xyz.zionjo.common.exception.BizCodeEnum;
import xyz.zionjo.common.to.SkuHasStockVo;
import xyz.zionjo.littlemall.ware.entity.WareSkuEntity;
import xyz.zionjo.littlemall.ware.service.WareSkuService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.ware.vo.LockStockResult;
import xyz.zionjo.littlemall.ware.vo.WareSkuLockVo;


/**
 * 商品库存
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:26:24
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 查询sku是否有库存
     *
     */
    @PostMapping("/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds){
        // sku_id, has_stock

        List<SkuHasStockVo> vos = wareSkuService.getSkusHasStock(skuIds);
        return R.ok().put("data",vos);
    }

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody  WareSkuLockVo vo){
        try {
            wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (Exception e){
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
