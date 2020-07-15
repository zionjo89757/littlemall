package xyz.zionjo.littlemall.member.dao;

import xyz.zionjo.littlemall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:07:23
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
