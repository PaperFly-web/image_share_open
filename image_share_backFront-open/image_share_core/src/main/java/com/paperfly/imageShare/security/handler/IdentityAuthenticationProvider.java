package com.paperfly.imageShare.security.handler;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自定义身份认证
 */
@Component
public class IdentityAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordEncoder pw = new BCryptPasswordEncoder();
        //获取UsernamePasswordSecurityFilter的attemptAuthentication方法传过来的身份与凭证
        //由于我在attemptAuthentication穿进去的是email，与password。所以按照下面的这样获取
        String email = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        UserEntity loginUser = userService.login(email);

        if (EmptyUtil.empty(loginUser)) {
            throw new BadCredentialsException("没有此用户");
        }
        if(loginUser.getState() == 1){
            throw new BadCredentialsException("您被禁止登录,请您联系管理员");
        }
        boolean checkPassword = pw.matches(password, loginUser.getPassword());
        if (!checkPassword) {
            throw new BadCredentialsException("密码或用户名不正确");
        }
        //更新用户登录时间
        UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("login_time",new Date());
        updateWrapper.eq("email",email);
        userService.update(updateWrapper);
        //把新的  身份与凭证 传递下去，代表数据库验证成功
        return new UsernamePasswordAuthenticationToken(loginUser, password);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
