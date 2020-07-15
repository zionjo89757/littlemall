package xyz.zionjo.littlemall.cart.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import xyz.zionjo.common.constant.AuthServerConstant;
import xyz.zionjo.common.constant.CartConstant;
import xyz.zionjo.common.vo.MemberVo;
import xyz.zionjo.littlemall.cart.vo.UserInfoTo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;


public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo user = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(memberVo != null){
            user.setUserId(memberVo.getId());
        }
        Cookie[] cookies = request.getCookies();

        if (cookies!=null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    user.setUserKey(cookie.getValue());
                    user.setTempUser(true);
                }

            }
        }
        if(StringUtils.isEmpty(user.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            user.setUserKey(uuid);
        }

        threadLocal.set(user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo user = threadLocal.get();
        if(!user.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, user.getUserKey());
            cookie.setDomain("littlemall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}