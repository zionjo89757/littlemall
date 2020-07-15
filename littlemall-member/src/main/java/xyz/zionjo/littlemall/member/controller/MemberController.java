package xyz.zionjo.littlemall.member.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.Mergeable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import xyz.zionjo.common.exception.BizCodeEnum;
import xyz.zionjo.common.to.MemberPrice;
import xyz.zionjo.littlemall.member.entity.MemberEntity;
import xyz.zionjo.littlemall.member.exception.PhoneExistException;
import xyz.zionjo.littlemall.member.exception.UsernameExistException;
import xyz.zionjo.littlemall.member.feign.CouponFeignService;
import xyz.zionjo.littlemall.member.service.MemberService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.member.vo.MemberLoginVo;
import xyz.zionjo.littlemall.member.vo.MemberRegistVo;
import xyz.zionjo.littlemall.member.vo.WeiboMemberVo;


/**
 * 会员
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 12:07:23
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;
    /**
     * feign测试
     */
    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("Google");

        R r = couponFeignService.member2coupon();
        return R.ok().put("member",memberEntity).put("coupons",r.get("coupons"));

    }
    /**
     * 注册
     */
    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegistVo){
        try{
            memberService.regist(memberRegistVo);
        }catch (PhoneExistException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }


        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity entity = memberService.login(vo);
        if(entity!= null){
            return R.ok().put("data",entity);

        }else {
            return R.error(BizCodeEnum.LOGINACCI_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCI_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody WeiboMemberVo vo){
        MemberEntity entity = memberService.oauthLogin(vo);
        if(entity!= null){
            return R.ok().put("data",entity);

        }else {
            return R.error(BizCodeEnum.LOGINACCI_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCI_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
