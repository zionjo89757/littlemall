package xyz.zionjo.littlemall.coupon.dao;

import xyz.zionjo.littlemall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 11:27:03
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
