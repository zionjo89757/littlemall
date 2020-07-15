package xyz.zionjo.littlemall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.ware.dao.WareInfoDao;
import xyz.zionjo.littlemall.ware.entity.WareInfoEntity;
import xyz.zionjo.littlemall.ware.feign.MemberFeignService;
import xyz.zionjo.littlemall.ware.service.WareInfoService;
import xyz.zionjo.littlemall.ware.vo.FareVo;
import xyz.zionjo.littlemall.ware.vo.MemberAddressVo;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key)
                        .or().like("name",key)
                        .or().like("address",key)
                        .or().like("areacode",key);

            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();

        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if(data != null){
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1);
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddress(data);
            fareVo.setFare(bigDecimal);
            return fareVo;

        }
        return null;
    }

}