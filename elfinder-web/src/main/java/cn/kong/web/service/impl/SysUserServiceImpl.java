package cn.kong.web.service.impl;

import cn.kong.web.dao.SysUserDao;
import cn.kong.web.entity.AuthToken;
import cn.kong.web.entity.SysUser;
import cn.kong.web.service.RedisService;
import cn.kong.web.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: gh
 * @date: 2021/03/23/0023 15:39
 * @description:
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUser> implements SysUserService {

    @Autowired
    RedisService redisService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SysUserDao sysUserDao;
    @Value("${server.port}")
    String port;
    @Value("${auth.ttl}")
    private int ttl;


    //   加盐保存密码
    @Override
    public void insertAdmin(SysUser sysUser) {
        String password = sysUser.getPassword();
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw(password, gensalt);
        sysUser.setPassword(hashpw);
        sysUserDao.insert(sysUser);
    }

    @Override
    public AuthToken login(SysUser sysUser, String clientId, String clientSecret) {
        String username = sysUser.getUsername();
        String password = sysUser.getPassword();
        //1.申请令牌
        String localHost = null;
//        try {
//            localHost =new String( InetAddress.getLocalHost().getHostName());
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
        localHost = "http://localhost:";
        String url = localHost + port + "/oauth/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", this.getHttpBasic(clientId, clientSecret));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = responseEntity.getBody();
        if (map == null || map.get("access_token") == null || map.get("refresh_token") == null || map.get("jti") == null) {
            //申请令牌失败
            throw new RuntimeException("申请令牌失败");
        }

        //2.封装结果数据
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken((String) map.get("access_token"));
        authToken.setRefreshToken((String) map.get("refresh_token"));
        authToken.setJti((String) map.get("jti"));

        //3.将jti作为redis中的key,将jwt作为redis中的value进行数据的存放
//        redisService.sSetAndTime(authToken.getJti(), ttl, authToken.getAccessToken());
        redisService.set(authToken.getJti(),ttl,authToken.getAccessToken());
        return authToken;
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String value = clientId + ":" + clientSecret;
        byte[] encode = Base64Utils.encode(value.getBytes());
        return "Basic " + new String(encode);
    }

    public static void main(String[] args) {
        SysUserServiceImpl sysUserService = new SysUserServiceImpl();
        String httpBasic = sysUserService.getHttpBasic("changgou", "changgou");
        System.out.println(httpBasic);
    }
}
