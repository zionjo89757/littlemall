package xyz.zionjo.littlemall.product.dao;

import xyz.zionjo.littlemall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 10:57:14
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
