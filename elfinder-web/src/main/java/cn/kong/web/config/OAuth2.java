package cn.kong.web.config;

import cn.kong.web.myenum.Authorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.stream.Collectors;


@Configuration
public class OAuth2 {

    /**
     * @description 资源配置，配置那些资源是需要授权访问，那些是放行的
     * @author xiaomianyang
     * @date 2019-06-25 13:04
     * @param 
     * @return 
     */
    @Configuration
    @EnableResourceServer
    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)//激活方法上的PreAuthorize注解
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Autowired
        private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @Autowired
        private CustomLogoutSuccessHandler customLogoutSuccessHandler;
        //公钥
        private static final String PUBLIC_KEY = "public.key";

        /***
         * 定义JwtTokenStore
         * @param jwtAccessTokenConverter
         * @return
         */
        @Bean
        @Autowired
        public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
            return new JwtTokenStore(jwtAccessTokenConverter);
        }

        //读取密钥的配置
     /*   encrypt:
        key-store:
        location: classpath:/changgou.jks
        secret: changgou
        alias: changgou
        password: changgou*/
        @Value("${encrypt.key-store.location}")
        String location;
        @Value("${encrypt.key-store.secret}")
        String secret;
        @Value("${encrypt.key-store.alias}")
        String alias;
        @Value("${encrypt.key-store.password}")
        String password;
        @Autowired
        private ResourceLoader resourceLoader;
        /***
         * 定义JJwtAccessTokenConverter
         * @return
         */
        @Bean
        public JwtAccessTokenConverter jwtAccessTokenConverter(CustomUserAuthenticationConverter customUserAuthenticationConverter) {
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
            Resource resource = resourceLoader.getResource(location);
            KeyPair keyPair = new KeyStoreKeyFactory(
                   resource ,                          //证书路径 changgou.jks
                    secret.toCharArray())              //证书秘钥 changgouapp
                    .getKeyPair(
                            alias,                     //证书别名 changgou
                            password.toCharArray());   //证书密码 changgou
            converter.setKeyPair(keyPair);
            //配置自定义的CustomUserAuthenticationConverter
            DefaultAccessTokenConverter accessTokenConverter = (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
            accessTokenConverter.setUserTokenConverter(customUserAuthenticationConverter);
            return converter;
        }
        /**
         * 获取非对称加密公钥 Key
         * @return 公钥 Key
         */
        private String getPubKey() {
            Resource resource = new ClassPathResource(PUBLIC_KEY);
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
                BufferedReader br = new BufferedReader(inputStreamReader);
                return br.lines().collect(Collectors.joining("\n"));
            } catch (IOException ioe) {
                return null;
            }
        }

        /***
         * Http安全配置，对每个到达系统的http请求链接进行校验
         * @param http
         * @throws Exception
         */
        @Override
        public void configure(HttpSecurity http) throws Exception {
            //所有请求必须认证通过
//            http.formLogin()
//                    .loginPage("/authentication/require")
//                    //登录需要经过的url请求
//                    .loginProcessingUrl("/authentication/form");

            http
                    .authorizeRequests()
                    .antMatchers("/user/*")
                    .authenticated()
                    .antMatchers("/oauth/token").permitAll()
                    .anyRequest()
                    .permitAll()
                    .and()
                    //关闭跨站请求防护
                    .csrf().disable();
        }
//        @Override
//        public void configure(HttpSecurity http) throws Exception {
//            http.exceptionHandling()
//                    .authenticationEntryPoint(customAuthenticationEntryPoint)
//                    .and()
//                    .logout()
//                    .logoutUrl("/oauth/logot")
//                    .logoutSuccessHandler(customLogoutSuccessHandler)
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers("/hello/").permitAll()
//                    .antMatchers("/secure/**").authenticated();
//        }
    }

    /**
     * @description 授权服务器配置，配置客户端id，密钥和令牌的过期时间
     * @author xiaomianyang
     * @date 2019-06-25 13:05
     * @param 
     * @return 
     */
    @Configuration
    @EnableAuthorizationServer
//    @PropertySource(value ="classpath:config.properties")
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Value("${auth.clientId}")
        private String clientid;

        @Value("${auth.clientSecret}")
        private String secret;

        @Value("${auth.tokenValidityInSeconds}")
        private int tokenValidityInSeconds;

        @Autowired
        private DataSource dataSource;

        //        @Bean
//        public TokenStore tokenStore(){
//            return new JdbcTokenStore(dataSource);
//        }
        @Autowired
        TokenStore tokenStore;
        @Autowired
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;
        //jwt令牌转换器
        @Autowired
        private JwtAccessTokenConverter jwtAccessTokenConverter;
        //SpringSecurity 用户自定义授权认证类
        @Autowired
        UserDetailsService userDetailsServiceImpl;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenStore(tokenStore)
                    .authenticationManager(authenticationManager)
                    .accessTokenConverter(jwtAccessTokenConverter)
                    .userDetailsService(userDetailsServiceImpl);;
        }
        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.allowFormAuthenticationForClients()
                    .passwordEncoder(NoOpPasswordEncoder.getInstance())
                    .tokenKeyAccess("permitAll()")
                    .checkTokenAccess("isAuthenticated()");
        }

        /**
         * @description 配置令牌的作用域和授权方式
         * @author xiaomianyang
         * @date 2019-06-25 13:07
         * @param [clients]
         * @return void
         */
        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                    .withClient(clientid)
                    .scopes("read","write")
//                    .authorities(Authorities.ROLE_ADMIN.name(), Authorities.ROLE_USER.name())
                    .authorizedGrantTypes("password","refresh_token")
                    .secret(secret)
                    .accessTokenValiditySeconds(tokenValidityInSeconds);
        }
    }

}