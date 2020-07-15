package xyz.zionjo.littlemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.coupon.entity.SkuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 11:27:02
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

