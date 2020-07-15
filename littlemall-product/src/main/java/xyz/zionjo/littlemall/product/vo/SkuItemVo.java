package xyz.zionjo.littlemall.product.vo;

import lombok.Data;
import xyz.zionjo.littlemall.product.entity.SkuImagesEntity;
import xyz.zionjo.littlemall.product.entity.SkuInfoEntity;
import xyz.zionjo.littlemall.product.entity.SpuInfoDescEntity;


import java.util.List;

@Data
public class SkuItemVo {

    SkuInfoEntity info;

    List<SkuImagesEntity> images;

    SpuInfoDescEntity desp;

    List<SkuItemSaleAttrVo> saleAttr;

    List<SpuItemAttrGroupVo> groupAttrs;

    boolean hasStock = true;


}
