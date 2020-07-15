package xyz.zionjo.littlemall.ware.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import xyz.zionjo.common.constant.WareConstant;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.littlemall.ware.dao.PurchaseDao;
import xyz.zionjo.littlemall.ware.entity.PurchaseDetailEntity;
import xyz.zionjo.littlemall.ware.entity.PurchaseEntity;
import xyz.zionjo.littlemall.ware.service.PurchaseDetailService;
import xyz.zionjo.littlemall.ware.service.PurchaseService;
import xyz.zionjo.littlemall.ware.service.WareSkuService;
import xyz.zionjo.littlemall.ware.vo.MergeVo;
import xyz.zionjo.littlemall.ware.vo.PurchaseDoneVo;
import xyz.zionjo.littlemall.ware.vo.PurchaseItemDoneVo;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired

    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);

    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATE.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());

            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // TODO 确认采购单是0或1才能继续执行
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity output = new PurchaseDetailEntity();
            output.setId(item);
            output.setPurchaseId(finalPurchaseId);
            output.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return output;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(finalPurchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);



    }

    @Transactional
    @Override
    public void received(List<Long> ids) {
        // 1 确定当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity output = this.getById(id);
            return output;
        }).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATE.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 2 改变采购单的状态
        this.updateBatchById(collect);
        // 3 改变采购项的状态

        collect.forEach(item ->{
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entity -> {
                PurchaseDetailEntity output = new PurchaseDetailEntity();
                output.setId(entity.getId());
                output.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return output;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Transactional
    @Override
    public void finished(PurchaseDoneVo purchaseDoneVo) {

        Long id = purchaseDoneVo.getId();
        // 2 改变采购项状态
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
            }else{
                // 3 将成功采购的商品入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());

            }
            detailEntity.setId(item.getItemId());
            detailEntity.setStatus(item.getStatus());
            updates.add(detailEntity);

        }
        purchaseDetailService.updateBatchById(updates);

        // 1 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setStatus(flag? WareConstant.PurchaseStatusEnum.FINISHED.getCode(): WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);




    }

}