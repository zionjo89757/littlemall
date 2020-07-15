package xyz.zionjo.littlemall.coupon.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.zionjo.common.to.MemberPrice;
import xyz.zionjo.common.to.SkuReductionTo;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.littlemall.coupon.dao.SkuFullReductionDao;
import xyz.zionjo.littlemall.coupon.entity.MemberPriceEntity;
import xyz.zionjo.littlemall.coupon.entity.SkuFullReductionEntity;
import xyz.zionjo.littlemall.coupon.entity.SkuLadderEntity;
import xyz.zionjo.littlemall.coupon.service.MemberPriceService;
import xyz.zionjo.littlemall.coupon.service.SkuFullReductionService;
import xyz.zionjo.littlemall.coupon.service.SkuLadderService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 1 保存优惠信息
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }


        // 2 满减打折信息

        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,reductionEntity);
        if(reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1){
            this.save(reductionEntity);
        }


        // 3 会员价格

        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity output = new MemberPriceEntity();
            output.setSkuId(skuReductionTo.getSkuId());
            output.setMemberLevelId(item.getId());
            output.setMemberLevelName(item.getName());
            output.setMemberPrice(item.getPrice());
            output.setAddOther(1);
            return output;
        }).filter(item -> item.getMemberPrice().compareTo(new BigDecimal("0")) == 1)
                .collect(Collectors.toList());

        memberPriceService.saveBatch(collect);

    }

}