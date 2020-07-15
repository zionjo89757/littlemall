package xyz.zionjo.littlemall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.to.SkuHasStockVo;
import xyz.zionjo.common.to.mq.OrderTo;
import xyz.zionjo.common.to.mq.StockDetailTo;
import xyz.zionjo.common.to.mq.StockLockedTo;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.ware.dao.WareSkuDao;
import xyz.zionjo.littlemall.ware.entity.WareOrderTaskDetailEntity;
import xyz.zionjo.littlemall.ware.entity.WareOrderTaskEntity;
import xyz.zionjo.littlemall.ware.entity.WareSkuEntity;
import xyz.zionjo.common.exception.NoStockException;
import xyz.zionjo.littlemall.ware.feign.OrderFeignService;
import xyz.zionjo.littlemall.ware.feign.ProductFeignService;
import xyz.zionjo.littlemall.ware.service.WareOrderTaskDetailService;
import xyz.zionjo.littlemall.ware.service.WareOrderTaskService;
import xyz.zionjo.littlemall.ware.service.WareSkuService;
import xyz.zionjo.littlemall.ware.vo.OrderItemVo;
import xyz.zionjo.littlemall.ware.vo.OrderVo;
import xyz.zionjo.littlemall.ware.vo.SkuWareHasStock;
import xyz.zionjo.littlemall.ware.vo.WareSkuLockVo;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;



    @Override
    public void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId){
        this.baseMapper.unlockStock(skuId, wareId,num);
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);

    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 1、判断是否为新纪录
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(wareSkuEntities == null || wareSkuEntities.size() == 0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);

            // TODO >>>远程查询sku的名字
            try {
                R r = productFeignService.info(skuId);
                if(r.getCode() == 0){
                    Map<String,Object> data = (Map<String, Object>) r.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
                log.error("商品服务查询异常:原因{}",e);
            }


            wareSkuEntity.setWareId(wareId);
            this.baseMapper.insert(wareSkuEntity);
        }else{
            this.baseMapper.addStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(item -> {
            SkuHasStockVo output = new SkuHasStockVo();
            // 查询当前sku的总库存量
            Long count = this.baseMapper.getSkuStock(item);
            output.setSkuId(item);
            output.setHasStock(count != null && count > 0L);
            return output;
        }).collect(Collectors.toList());
        return collect;
    }

    @Transactional
    @Override
    public void orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存工作单的详情
         * 追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = this.baseMapper.listWareIdsHasSkuStock(skuId);
            stock.setWareIds(wareIds);

            return stock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareIds();
            Integer num = stock.getNum();
            if(wareIds == null || wareIds.size() == 0){
                throw new NoStockException();
            }
            for (Long wareId : wareIds) {
                Long col = this.baseMapper.lockSkuStock(skuId,wareId,num);
                if(col == 1){
                    skuStocked = true;
                    // TODO 通知MQ库存锁定成功
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setSkuId(skuId);
                    wareOrderTaskDetailEntity.setSkuNum(num);
                    wareOrderTaskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                    wareOrderTaskDetailEntity.setWareId(wareId);
                    wareOrderTaskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,detailTo);
                    lockedTo.setDetail(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedTo);

                    break;
                }
            }
            if(skuStocked == false){
                throw new NoStockException();
            }
        }
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if(byId != null){
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // TODO 查订单状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode()==0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if(data == null || data.getStatus() == 4){
                    if(detail.getLockStatus() == 1)
                    unlockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                }
            }else{
                throw new RuntimeException("远程服务失败");
            }

        }
    }

    @Transactional
    @Override
    public void unlockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        // 查库存解锁状态
        WareOrderTaskEntity taskEntity =  wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = taskEntity.getId();
        // 没有解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unlockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());

        }

    }


}