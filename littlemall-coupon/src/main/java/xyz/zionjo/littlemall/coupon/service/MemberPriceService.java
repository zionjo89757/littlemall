package xyz.zionjo.littlemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.coupon.entity.MemberPriceEntity;

import java.util.Map;

/**
 * 商品会员价格
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 11:27:03
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

