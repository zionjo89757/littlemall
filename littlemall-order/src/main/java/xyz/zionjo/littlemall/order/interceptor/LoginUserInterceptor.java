package xyz.zionjo.littlemall.order.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.zionjo.common.constant.AuthServerConstant;
import xyz.zionjo.common.vo.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberVo> loginUser = new ThreadLocal<>();



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", requestURI);
        if(match) return true;

        MemberVo attribute = (MemberVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.littlemall.com/login.html");
            return false;
        }else{
            loginUser.set(attribute);
            return true;
        }


    }
}
