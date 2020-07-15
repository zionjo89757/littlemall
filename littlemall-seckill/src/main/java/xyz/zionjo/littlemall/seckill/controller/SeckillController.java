package xyz.zionjo.littlemall.seckill.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.seckill.service.SeckillService;
import xyz.zionjo.littlemall.seckill.to.SecKillSkuRedisTo;

import java.security.PublicKey;
import java.util.List;
@Slf4j
@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data",vos);
    }

    @GetMapping("/kill")
    public R secKill(@RequestParam("killId") String killId,@RequestParam("key") String key,@RequestParam("num") Integer num){
        log.info("进入秒杀>>>商品：{}",killId);
        String orderSn = seckillService.kill(killId,key,num);
        return R.ok().put("data",orderSn);
    }
}
