package xyz.zionjo.littlemall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.littlemall.coupon.dao.SeckillSkuRelationDao;
import xyz.zionjo.littlemall.coupon.entity.SeckillSkuRelationEntity;
import xyz.zionjo.littlemall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> wrapper = new QueryWrapper<>();
        String sessionId = (String) params.get("promotionSessionId");
        if (!StringUtils.isEmpty(sessionId)) {
            wrapper.eq("promotion_session_id",sessionId);
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}