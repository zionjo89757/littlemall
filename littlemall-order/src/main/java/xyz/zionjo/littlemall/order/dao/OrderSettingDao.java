package xyz.zionjo.littlemall.order.dao;

import xyz.zionjo.littlemall.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:20:03
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
