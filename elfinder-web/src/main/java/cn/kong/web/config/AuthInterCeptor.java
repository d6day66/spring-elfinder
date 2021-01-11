package cn.kong.web.config;

import cn.kong.elfinder.util.UrlFilter;
import cn.kong.web.service.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: gh
 * @date: 2021/04/04/0004 19:31
 * @description:
 */
@Component
public class AuthInterCeptor implements HandlerInterceptor {
    @Autowired
    AuthService authService;
    private static final String LOGIN_URL="http://localhost:8080/toLogin";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURL = request.getRequestURI();
        String servletPath = request.getServletPath();
        String contextPath = request.getContextPath();
        System.out.println("AuthInterCeptor.class : " + requestURL);
        if ("/login".equals(requestURL)||"/toLogin".equals(requestURL)||"/login.html".equals(requestURL)||!UrlFilter.hasAuthorize(requestURL)) {
            return true;
        }
        //2.从cookie中获取jti的值,如果该值不存在,拒绝本次访问
        String jti = authService.getJtiFromCookie(request);
        System.out.println("cookie中jti为：" + jti);
        if (StringUtils.isEmpty(jti)){
            //拒绝访问
            /*response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
            //跳转登录页面
            System.out.println("跳转地址：" + LOGIN_URL);
            return toLoginPage(LOGIN_URL+"?FROM="+request.getRequestURL(),response);
        }

        //3.从redis中获取jwt的值,如果该值不存在,拒绝本次访问
        String jwt = authService.getJwtFromRedis(jti);
        System.out.println("redis中jwt值为："+ jwt);
        if (StringUtils.isEmpty(jwt)){
            //拒绝访问
            /*response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
            return toLoginPage(LOGIN_URL+"?FROM="+request.getRequestURL(),response);
        }

        //4.对当前的请求对象进行增强,让它会携带令牌的信息
//        request.setAttribute("Authorization","Bearer "+jwt);
//        System.out.println("request参数：" + request.getAttribute("Authorization"));
        return true;
    }
    private boolean toLoginPage(String loginUrl, HttpServletResponse response) {
        try {
            response.sendRedirect(loginUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.isCommitted();
    }
}
