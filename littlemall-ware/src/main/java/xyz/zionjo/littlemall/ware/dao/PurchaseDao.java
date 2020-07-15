package xyz.zionjo.littlemall.ware.dao;

import xyz.zionjo.littlemall.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:26:24
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
