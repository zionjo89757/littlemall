package xyz.zionjo.littlemall.product.dao;

import org.apache.ibatis.annotations.Param;
import xyz.zionjo.littlemall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.zionjo.littlemall.product.vo.SkuItemVo;
import xyz.zionjo.littlemall.product.vo.SpuItemAttrGroupVo;

import java.util.List;

/**
 * 属性分组
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 10:57:14
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
