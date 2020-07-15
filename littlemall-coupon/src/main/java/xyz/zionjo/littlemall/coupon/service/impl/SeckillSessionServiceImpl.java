package xyz.zionjo.littlemall.coupon.service.impl;

import com.mysql.cj.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.channels.SeekableByteChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.littlemall.coupon.dao.SeckillSessionDao;
import xyz.zionjo.littlemall.coupon.entity.SeckillSessionEntity;
import xyz.zionjo.littlemall.coupon.entity.SeckillSkuRelationEntity;
import xyz.zionjo.littlemall.coupon.service.SeckillSessionService;
import xyz.zionjo.littlemall.coupon.service.SeckillSkuRelationService;

@Slf4j
@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        log.info("搜索秒杀数据");
        //Date date = new Date();

        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));

        if(list!=null && list.size()>0){
            log.info("有秒杀数据了");
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntities = relationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationSkus(relationEntities);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private String startTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now,min);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;

    }

    private String endTime(){
        LocalDate day = LocalDate.now().plusDays(2);
        LocalDateTime dateTime = LocalDateTime.of(day, LocalTime.MAX);
        String format = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;


    }

}