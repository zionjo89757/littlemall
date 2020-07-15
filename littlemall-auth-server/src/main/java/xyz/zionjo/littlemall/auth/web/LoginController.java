package xyz.zionjo.littlemall.auth.web;

import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.zionjo.common.constant.AuthServerConstant;
import xyz.zionjo.common.exception.BizCodeEnum;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.common.vo.MemberVo;
import xyz.zionjo.littlemall.auth.feign.MemberFeignService;
import xyz.zionjo.littlemall.auth.feign.ThirdPartyFeignService;
import xyz.zionjo.littlemall.auth.vo.UserLoginVo;
import xyz.zionjo.littlemall.auth.vo.UserRegistVo;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        if(session.getAttribute(AuthServerConstant.LOGIN_USER) != null){
            return "redirect:http:/littlemall.com";
        }else{

            return "login";
        }
    }

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        // TODO 接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            long time = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - time < 60000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        // 2 验证码的再次校验
        String code = UUID.randomUUID().toString().substring(0, 6);
        String code_time = code + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code_time,10, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone,code);

        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session原理，分布式可能存在问题
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String register(@Valid  UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        // 校验
        if(result.hasErrors()){
            Map<String, String> collect = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",collect);
            return "redirect:http://auth.littlemall.com/reg.html";
        }

        // 校验验证码
        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(!StringUtils.isEmpty(redisCode) && code.equals(redisCode.split("_")[0])){
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
            // TODO 调用远程服务进行注册
            R r = memberFeignService.regist(vo);
            if(r.getCode() == 0){
                return "redirect:http://auth.littlemall.com/login.html";
            }else{
                Map<String,String> errors = new HashMap<>();
                errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.littlemall.com/reg.html";
            }

        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littlemall.com/reg.html";
        }

    }


    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){

        R r = memberFeignService.login(vo);
        if(r.getCode() == 0){
            MemberVo memberVo = r.getData(new TypeReference<MemberVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER,memberVo);

            return "redirect:http://littlemall.com";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littlemall.com/login.html";
        }



    }
}
