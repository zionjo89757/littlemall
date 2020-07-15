package xyz.zionjo.littlemall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.member.entity.MemberEntity;
import xyz.zionjo.littlemall.member.exception.PhoneExistException;
import xyz.zionjo.littlemall.member.exception.UsernameExistException;
import xyz.zionjo.littlemall.member.vo.MemberLoginVo;
import xyz.zionjo.littlemall.member.vo.MemberRegistVo;
import xyz.zionjo.littlemall.member.vo.WeiboMemberVo;

import java.util.Map;

/**
 * 会员
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:07:23
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo memberRegistVo);

    void checkUsernameUnique(String name) throws UsernameExistException;

    void checkMobileUnique(String mobile) throws PhoneExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity oauthLogin(WeiboMemberVo vo);
}

