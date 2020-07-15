package xyz.zionjo.littlemall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.to.mq.QuickOrderTo;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.common.vo.MemberVo;
import xyz.zionjo.littlemall.seckill.feign.CouponFeignService;
import xyz.zionjo.littlemall.seckill.feign.ProductFeignService;
import xyz.zionjo.littlemall.seckill.interceptor.LoginUserInterceptor;
import xyz.zionjo.littlemall.seckill.service.SeckillService;
import xyz.zionjo.littlemall.seckill.to.SecKillSkuRedisTo;
import xyz.zionjo.littlemall.seckill.vo.SeckillSessionsWithSkus;
import xyz.zionjo.littlemall.seckill.vo.SeckillSkuVo;
import xyz.zionjo.littlemall.seckill.vo.SkuInfoVo;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private static final String SKUKILL_CACHE_PREFIX = "seckill:sku";
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";


    @Override
    public void uploadSeckillSkuLatest3Days() {
        // TODO 远程调用 扫描最近三天需要参与秒杀的活动
        List<SeckillSessionsWithSkus> data = new ArrayList<>();
        R r = couponFeignService.getLatest3DaySession();

        data = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
        });
        if(data == null || data.size() == 0){
            log.info("没有商品");
            return;
        }


        // 缓存到redis
        // 缓存活动信息
        saveSessionInfos(data);
        // 缓存活动的关联商品信息
        saveSessionSkuInfos(data);



    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        // 1 确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            Long start = Long.parseLong(s[0]);
            Long end = Long.parseLong(s[1]);
            if(time>=start && time<=end){

                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if(list != null){
                    List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                        SecKillSkuRedisTo redisTo = JSON.parseObject(item, SecKillSkuRedisTo.class);
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }

        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {

        MemberVo memberVo = LoginUserInterceptor.loginUser.get();

        log.info("商品详细信息");
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOps.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        SecKillSkuRedisTo redisTo = JSON.parseObject(s, SecKillSkuRedisTo.class);
        log.info("检验合法性");
        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        long time = new Date().getTime();
        long ttl = endTime - time;
        log.info("检验时间合法性");
        if(!(time>= startTime && time<=endTime)){
            return null;
        }
        log.info("检验随机码");
        String randomCode = redisTo.getRandomCode();
        if (!randomCode.equals(key)) {
            return null;
        }
        log.info("检验购物数量");
        if(num>redisTo.getSeckillLimit().intValue()){
            return null;
        }
        log.info("是否已经购买过");
        String redisKey = memberVo.getId() + "_" + killId;
        // 自动过期
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
        if(!aBoolean) {
            return null;
        }
        log.info("减分布式信号量");
        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
        try {
            boolean b = semaphore.tryAcquire(num);
            if(b){
                log.info("快速下单，发送MQ消息");
                String timeId = IdWorker.getTimeId();
                QuickOrderTo orderTo = new QuickOrderTo();
                orderTo.setMemberId(memberVo.getId());
                orderTo.setOrderSn(timeId);
                orderTo.setNum(num);
                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);

                return timeId;
            }
            return null;


        } catch (Exception e) {
            return null;
        }

    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions){
        sessions.stream().forEach(session ->{
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key =SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = redisTemplate.hasKey(key);

            if(!hasKey){

                List<String> val = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId()+"-"+item.getSkuId()).collect(Collectors.toList());
                if(val != null && val.size() > 0)redisTemplate.opsForList().leftPushAll(key,val);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        sessions.stream().forEach(session -> {

            session.getRelationSkus().stream().forEach( sku ->{

                String token = UUID.randomUUID().toString().replace("-", "");

                if (!hashOps.hasKey(sku.getPromotionSessionId()+"-"+sku.getSkuId().toString())) {
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    // sku 的基本信息
                    R r = productFeignService.skuInfo(sku.getSkuId());
                    SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    redisTo.setSkuInfoVo(skuInfo);

                    // sku 的秒杀信息

                    BeanUtils.copyProperties(sku,redisTo);

                    // 设置当前商品的秒杀信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    // 随机码


                    redisTo.setRandomCode(token);


                    String s = JSON.toJSONString(redisTo);
                    hashOps.put(sku.getPromotionSessionId()+"-"+sku.getSkuId().toString(),s);

                    // 库存信号量 限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(sku.getSeckillCount().intValue());
                }
            });
        });
    }
}
