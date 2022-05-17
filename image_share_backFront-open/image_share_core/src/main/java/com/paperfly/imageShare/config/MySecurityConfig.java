package com.paperfly.imageShare.config;

import com.paperfly.imageShare.common.constant.JwtProperties;
import com.paperfly.imageShare.security.handler.MyAccessDeniedHandler;
import com.paperfly.imageShare.security.handler.MyLogoutHandler;
import com.paperfly.imageShare.security.BasicSecurityFilter;
import com.paperfly.imageShare.security.handler.NoLoginAuthenticationEntryPoint;
import com.paperfly.imageShare.security.handler.IdentityAuthenticationProvider;
import com.paperfly.imageShare.security.UsernamePasswordSecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsUtils;

/**
 * @EnableGlobalMethodSecurity: 开启注解方式
*@desc:
*@param:* @param null:
*@return:* @return: null
*@author:paperfly
*@time:2020/8/21 18:29
*/
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MySecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    IdentityAuthenticationProvider identityAuthenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth){
        //添加自定义密码校验
        auth.authenticationProvider(identityAuthenticationProvider);
    }

    //给资源授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //设置权限管理规则
        http.authorizeRequests()
                //允许匿名访问的链接
                .antMatchers("/login").permitAll()
                .antMatchers("/open/**").permitAll()
                .antMatchers("/ws/**").permitAll()
                .antMatchers("/admin/**").hasAnyAuthority("1","2")
                .antMatchers("/superAdmin/**").hasAnyAuthority("2")
                .antMatchers(HttpMethod.POST,"/user").permitAll()
                .antMatchers(HttpMethod.PUT,"/user/findPassword").permitAll()
                //其他所有请求需要身份认证
                .anyRequest().authenticated()
                .and()
                // 添加JWT登录拦截器,这个过滤器是继承UsernamePasswordAuthenticationFilter，所以就不使用它默认的用户密码过滤器，就是使用我们自己的
                .addFilter(new UsernamePasswordSecurityFilter(authenticationManager()))
                //添加JWT鉴权拦截器，同理
                .addFilter(new BasicSecurityFilter(authenticationManager()))
                .sessionManagement()
                // 设置Session的创建策略为：Spring Security永不创建HttpSession 不使用HttpSession来获取SecurityContext
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 异常处理
                .exceptionHandling()
                //没有权限访问
                .accessDeniedHandler(new MyAccessDeniedHandler())
                //用户未登录
                .authenticationEntryPoint(new NoLoginAuthenticationEntryPoint());

        http.csrf()
                //关闭网站攻击
                .disable();
        //开启跨域请求
        http.cors();
        http.formLogin()
                .passwordParameter(JwtProperties.USER_EMAIL_COL_NAME)
                .usernameParameter(JwtProperties.USER_PWD_COL_NAME);

        http.logout()
                .logoutUrl("/logout")
                //退出登录
                .logoutSuccessHandler(new MyLogoutHandler());

    }

    //注入密码加密的算法
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


}
