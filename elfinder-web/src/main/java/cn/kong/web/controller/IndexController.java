package cn.kong.web.controller;

import cn.kong.elfinder.util.CookieUtil;
import cn.kong.web.annotation.AccessLimit;
import cn.kong.web.entity.AuthToken;
import cn.kong.web.entity.Result;
import cn.kong.web.entity.SysUser;
import cn.kong.web.myenum.ResultCode;
import cn.kong.web.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
public class IndexController {
    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;
    @Autowired
    SysUserService sysUserService;

    @GetMapping(value = {"/toLogin"})
    @AccessLimit(seconds = 4, maxCount = 2)
    public String login() {
        return "login";
    }

    @RequestMapping("/login")
    @ResponseBody
    @AccessLimit(seconds = 4, maxCount = 2)
    public cn.kong.web.entity.Result login(String username, String password, HttpServletResponse response) {
//        String username = sysUser.getUsername();
//        String password = sysUser.getPassword();
        //校验参数
        if (StringUtils.isEmpty(username)) {
            return Result.failed(ResultCode.FAILED, "请输入用户名");
        }
        if (StringUtils.isEmpty(password)) {
            return Result.failed(ResultCode.FAILED, "请输入密码");
        }
        AuthToken authToken = null;
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(password);
        try {
            //申请令牌 authtoken
            authToken = sysUserService.login(sysUser, clientId, clientSecret);

            //将jti的值存入cookie中
            this.saveJtiToCookie(authToken.getJti(), response);

            //返回结果
            return cn.kong.web.entity.Result.success(authToken.getJti(), "登陆成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failed(ResultCode.FAILED, "登陆失败");
    }

    //将令牌的断标识jti存入到cookie中
    private void saveJtiToCookie(String jti, HttpServletResponse response) {
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", jti, cookieMaxAge, false);
    }
}
