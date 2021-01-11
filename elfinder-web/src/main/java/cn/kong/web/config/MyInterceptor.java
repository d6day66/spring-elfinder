package cn.kong.web.config;

import cn.kong.web.annotation.AccessLimit;
import cn.kong.web.entity.AccessKey;
import cn.kong.web.entity.Result;
import cn.kong.web.myenum.ResultCode;
import cn.kong.web.service.RedisService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: gh
 * @date: 2021/03/21/0021 17:10
 * @description:
 */
@Component
public class MyInterceptor implements HandlerInterceptor {
    @Autowired
    RedisService redisService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit mt = hm.getMethodAnnotation(AccessLimit.class);
            if (mt == null) {
                return true;
            }
            String key = request.getRequestURI();
            int max = mt.maxCount();
            int seconds = mt.seconds();
            boolean login = mt.needLogin();
            if (login) {
                key += key + "" + "1";
            }
            AccessKey accessKey = AccessKey.withExpire(seconds);
            Integer count = (Integer) redisService.get(key);
            if (count == null) {
                int time = accessKey.getAk();
                redisService.set(key,time,1);
            } else if (count < max) {
                redisService.incr(key, 1);
            } else {
                redener(response, ResultCode.FREQUENTLY);
                return false;
            }
        }
        return true;
    }

    private void redener(HttpServletResponse response, ResultCode fm) throws IOException {
        String str = JSON.toJSONString(Result.failed(fm));
        response.setContentType("application/json;charset=utf-8");
//        response.sendError(502,str);
//        response.setStatus(HttpStatus.SEE_OTHER.value());
//        PrintWriter writer = response.getWriter();
//        writer.println(str);
        System.out.println(str);
        response.sendRedirect("error.html");
    }
}
