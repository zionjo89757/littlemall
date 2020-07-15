package xyz.zionjo.littlemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.ware.entity.WareInfoEntity;
import xyz.zionjo.littlemall.ware.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:26:24
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long id);
}

