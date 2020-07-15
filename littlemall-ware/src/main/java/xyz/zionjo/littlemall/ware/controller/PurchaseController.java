package xyz.zionjo.littlemall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import feign.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import xyz.zionjo.littlemall.ware.entity.PurchaseEntity;
import xyz.zionjo.littlemall.ware.service.PurchaseService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.ware.vo.MergeVo;
import xyz.zionjo.littlemall.ware.vo.PurchaseDoneVo;
import xyz.zionjo.littlemall.ware.vo.PurchaseItemDoneVo;


/**
 * 采购信息
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:26:24
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 查询未领取的采购单
     * /ware/purchase/unreceive/list
     */
    @GetMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceivePurchase(params);

        return R.ok().put("page", page);
    }

    /**
     * 合并采购需求
     * /ware/purchase/merge
     */
    @PostMapping("/merge")
    //@RequiresPermissions("ware:purchase:list")
    public R merge(@RequestBody MergeVo mergeVo){
        purchaseService.mergePurchase(mergeVo);

        return R.ok();
    }

    /**
     * 领取采购单
     * /ware/purchase/received
     */
    @PostMapping("/received")
    //@RequiresPermissions("ware:purchase:list")
    public R received(@RequestBody List<Long> ids){
        purchaseService.received(ids);

        return R.ok();
    }

    /**
     * /ware/purchase/done
     * 完成采购
     */
    @PostMapping("/done")
    //@RequiresPermissions("ware:purchase:list")
    public R finished(@RequestBody PurchaseDoneVo purchaseDoneVo){
        purchaseService.finished(purchaseDoneVo);

        return R.ok();
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
