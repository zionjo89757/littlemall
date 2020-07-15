package xyz.zionjo.littlemall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.constant.ProductConstant;
import xyz.zionjo.common.to.SkuHasStockVo;
import xyz.zionjo.common.to.SkuReductionTo;
import xyz.zionjo.common.to.SpuBoundTo;
import xyz.zionjo.common.to.es.SkuEsModel;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.product.dao.SpuInfoDao;
import xyz.zionjo.littlemall.product.entity.*;
import xyz.zionjo.littlemall.product.feign.CouponFeignService;
import xyz.zionjo.littlemall.product.feign.SearchFeignService;
import xyz.zionjo.littlemall.product.feign.WareFeignService;
import xyz.zionjo.littlemall.product.service.*;
import xyz.zionjo.littlemall.product.vo.*;

import javax.swing.text.html.parser.Entity;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    // TODO 高级部分再来完整
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        // 1 保存Spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2 保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        // 3 保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        // 4 保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity output = new ProductAttrValueEntity();
            output.setAttrId(item.getAttrId());
            AttrEntity byId = attrService.getById(item.getAttrId());
            output.setAttrName(byId.getAttrName());
            output.setAttrValue(item.getAttrValues());
            output.setQuickShow(item.getShowDesc());
            output.setSpuId(spuInfoEntity.getId());
            return output;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);
        // 5 TODO >>>保存spu的积分信息 littlemall_sms:sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("保存远程spu积分信息失败");
        }

        // 6 保存当前spu对应得所有sku信息
        List<Skus> skus = vo.getSkus();


        if(skus != null && skus.size() > 0){
            skus.forEach(item -> {
                String defaultImg = "";
                for(Images image : item.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }

                // 6.1 sku的基本信息 pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                // 6.2 sku的图片信息 pms_sku_images
                // 没有图片路径的无需保存
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity output = new SkuImagesEntity();
                    output.setSkuId(skuId);
                    output.setDefaultImg(img.getDefaultImg());
                    output.setImgUrl(img.getImgUrl());
                    return output;
                }).filter(entity -> !StringUtils.isEmpty(entity.getImgUrl())
                ).collect(Collectors.toList());

                skuImagesService.saveBatch(imagesEntities);

                // 6.3 sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();

                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity output = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, output);
                    output.setSkuId(skuId);
                    return output;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                // 6.4 TODO >>>sku的优惠、满减信息 littlemall_sms:sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("保存远程sku优惠信息失败");
                    }
                }

            });
        }




    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catalogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }


    /**
     * 商品上架
     * @param spuId
     */
    @Override
    public void up(Long spuId) {

        //组装需要的数据

        // 查询当前Sku的所有可以被检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attr> attrList = baseAttrs.stream().filter(
                item -> idSet.contains(item.getAttrId())
        ).map(item -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());



        // 1 查出当前spu_id对应的所有sku信息。
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        // TODO >>>发送远程调用，库存系统是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIdList);
            stockMap = r.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));

        }catch (Exception e){
            log.error("库存服务查询异常:原因{}",e);
        }



        // 2 封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(item -> {
            SkuEsModel output = new SkuEsModel();
            BeanUtils.copyProperties(item,output);
            output.setSkuPrice(item.getPrice());
            output.setSkuImg(item.getSkuDefaultImg());

            if(finalStockMap == null){
                output.setHasStock(true);
            }else{
                output.setHasStock(finalStockMap.get(item.getSkuId()));
            }

            // TODO 热度评分，默认0
            output.setHotScore(0L);

            BrandEntity brandEntity = brandService.getById(item.getBrandId());
            output.setBrandImg(brandEntity.getLogo());
            output.setBrandName(brandEntity.getName());

            CategoryEntity categoryEntity = categoryService.getById(item.getCatalogId());
            output.setCatalogName(categoryEntity.getName());

            // 设置检索属性
            output.setAttrs(attrList);

            return output;
        }).collect(Collectors.toList());

        // TODO >>>将数据发送给es进行保存
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode() == 0){
            //远程调用成功，修改spu状态
            this.baseMapper.updateStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            //远程调用失败
            //TODO 重复调用？接口幂等性
            log.error("调用搜索服务失败");
        }

    }

    @Override
    public SpuInfoEntity getBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        return getById(spuId);
    }


}