package xyz.zionjo.littlemall.seckill.service;

import xyz.zionjo.littlemall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    String kill(String killId, String key, Integer num);
}
