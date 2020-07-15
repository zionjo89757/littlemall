package xyz.zionjo.littlemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.ware.entity.PurchaseEntity;
import xyz.zionjo.littlemall.ware.vo.MergeVo;
import xyz.zionjo.littlemall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:26:24
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void finished(PurchaseDoneVo purchaseDoneVo);
}

