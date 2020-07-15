package xyz.zionjo.littlemall.product.dao;

import org.apache.ibatis.annotations.Param;
import xyz.zionjo.littlemall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.zionjo.littlemall.product.vo.SkuItemSaleAttrVo;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 10:57:14
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValues(@Param("skuId") Long skuId);
}
