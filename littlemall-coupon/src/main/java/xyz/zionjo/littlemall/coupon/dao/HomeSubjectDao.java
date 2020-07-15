package xyz.zionjo.littlemall.coupon.dao;

import xyz.zionjo.littlemall.coupon.entity.HomeSubjectEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 11:27:03
 */
@Mapper
public interface HomeSubjectDao extends BaseMapper<HomeSubjectEntity> {
	
}
