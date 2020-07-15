package xyz.zionjo.littlemall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.littlemall.product.dao.SkuSaleAttrValueDao;
import xyz.zionjo.littlemall.product.entity.SkuSaleAttrValueEntity;
import xyz.zionjo.littlemall.product.service.SkuSaleAttrValueService;
import xyz.zionjo.littlemall.product.vo.SkuItemSaleAttrVo;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {

        List<SkuItemSaleAttrVo> saleAttrVos = this.baseMapper.getSaleAttrsBySpuId(spuId);
        return saleAttrVos;
    }

    @Override
    public List<String> getSkuSaleAttrValues(Long skuId) {
        return this.baseMapper.getSkuSaleAttrValues(skuId);
    }

}