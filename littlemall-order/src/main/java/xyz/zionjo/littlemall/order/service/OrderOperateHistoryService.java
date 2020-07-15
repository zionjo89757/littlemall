package xyz.zionjo.littlemall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:20:03
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

