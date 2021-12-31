package com.vienna.jaray.auth.config;

import com.vienna.jaray.auth.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import java.util.Collections;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    //http://localhost:9009/oauth/authorize?client_id=clientId&response_type=code&redirect_uri=http://www.baidu.com&scope=all

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public TokenStore redisTokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                // 客户端得ID,比如我们在QQ互联中心申请得。可以写多个。配置 循环
                .withClient("clientId")
                // 客户端密钥，需要进行加密
                .secret(passwordEncoder().encode("secret"))
                // token 有效时常  0 永久有效
                .accessTokenValiditySeconds(7200)
                // 支持得授权类型
                .authorizedGrantTypes("password", "implicit", "refresh_token", "authorization_code")
                // 回调地址
                .redirectUris("http://www.baidu.com")
                // 拥有的 scope  可选
                .scopes("all", "read", "write");
    }

    @Bean
    public AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        // 认证管理器
        tokenServices.setAuthenticationManager(authenticationManager());
        // 客户端信息，添加后没有了刷新token
        // tokenServices.setClientDetailsService(clientDetailsService);
        //redis token存储
        tokenServices.setTokenStore(redisTokenStore());
        //token增强
        TokenEnhancerChain tokenEnhancer = new TokenEnhancerChain();
        tokenEnhancer.setTokenEnhancers(Collections.singletonList(jwtAccessTokenConverter));
        tokenServices.setTokenEnhancer(tokenEnhancer);
        //支持刷新token
        tokenServices.setSupportRefreshToken(true);
        //token 有效期 2小时
        tokenServices.setAccessTokenValiditySeconds(2 * 60 * 60);
        // 刷新token有效期 7天
        tokenServices.setRefreshTokenValiditySeconds(7 * 24 * 60 * 60);
        return tokenServices;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        endpoints
                // 用户信息得服务，一版是都数据库
                .userDetailsService(userDetailsService())
                // token
                .tokenServices(tokenServices())
                // 认证管理器
                .authenticationManager(authenticationManager())

                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                // 允许表单登录
                .allowFormAuthenticationForClients()
                // 开启/oauth/check_token验证端口认证权限访问
                .checkTokenAccess("isAuthenticated()")
                .tokenKeyAccess("permitAll()");
    }

    /**
     * 注入 认证管理器
     * @return
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        AuthenticationManager authenticationManager = new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return daoAuthenticationProvider().authenticate(authentication);
            }
        };
        return authenticationManager;
    }

    /**
     * 注入 认证器
     * @return
     */
    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * 注入 用户信息服务
     * @return
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    /**
     * 注入 密码加密
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
