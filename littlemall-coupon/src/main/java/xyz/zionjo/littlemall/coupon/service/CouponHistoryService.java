package xyz.zionjo.littlemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.coupon.entity.CouponHistoryEntity;

import java.util.Map;

/**
 * 优惠券领取历史记录
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 11:27:03
 */
public interface CouponHistoryService extends IService<CouponHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

