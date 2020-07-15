package xyz.zionjo.littlemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.to.SkuHasStockVo;
import xyz.zionjo.common.to.mq.OrderTo;
import xyz.zionjo.common.to.mq.StockLockedTo;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.ware.entity.WareSkuEntity;
import xyz.zionjo.littlemall.ware.vo.LockStockResult;
import xyz.zionjo.littlemall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:26:24
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId);

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    void orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo to);
}

