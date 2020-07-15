package xyz.zionjo.littlemall.coupon.dao;

import xyz.zionjo.littlemall.coupon.entity.SkuLadderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品阶梯价格
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 11:27:02
 */
@Mapper
public interface SkuLadderDao extends BaseMapper<SkuLadderEntity> {
	
}
