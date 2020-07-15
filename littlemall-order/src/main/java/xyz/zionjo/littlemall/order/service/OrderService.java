package xyz.zionjo.littlemall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.to.mq.QuickOrderTo;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.order.entity.OrderEntity;
import xyz.zionjo.littlemall.order.vo.OrderConfirmVo;
import xyz.zionjo.littlemall.order.vo.OrderSubmitVo;
import xyz.zionjo.littlemall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:20:03
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    void createSeckillOrder(QuickOrderTo orderTo);
}

