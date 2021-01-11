package cn.kong.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class AuthService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
//从cookie中获取jti
    public String getJtiFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("uid".equals(cookie.getName())){
                String jti = cookie.getValue();
                return jti;
            }
        }

        return null;
    }
    public String getJwtFromRedis(String jti){
        String jwt = stringRedisTemplate.boundValueOps(jti).get();
        return jwt;
    }
}
