package xyz.zionjo.littlemall.seckill.to;

import lombok.Data;
import xyz.zionjo.littlemall.seckill.vo.SkuInfoVo;

import java.math.BigDecimal;

@Data
public class SecKillSkuRedisTo {

    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    private SkuInfoVo skuInfoVo;

    private Long startTime;

    private Long endTime;

    private String randomCode;

}
