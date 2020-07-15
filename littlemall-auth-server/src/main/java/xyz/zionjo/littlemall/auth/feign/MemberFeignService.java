package xyz.zionjo.littlemall.auth.feign;

import org.apache.catalina.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.auth.vo.UserLoginVo;
import xyz.zionjo.littlemall.auth.vo.UserRegistVo;
import xyz.zionjo.littlemall.auth.vo.WeiboUserVo;

@FeignClient("littlemall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo UserRegistVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody WeiboUserVo vo);
}
