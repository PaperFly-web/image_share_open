package com.paperfly.imageShare.service.impl;

import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.constant.JwtProperties;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.service.MyJwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("myJwtTokenService")
@Slf4j
public class MyJwtTokenServiceImpl implements MyJwtTokenService {
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public R parseToken(String token) {
        String sec = (String) redisTemplate.opsForValue().get(token);
        token = token.split("\\|-_-\\|")[0].replace(JwtProperties.USER_TOKEN_PREFIX, "");
        // 判断有没有token
        if (EmptyUtil.empty(token)) {
            return R.error().put("code", CodeConstant.NOT_FOUND).put("msg", "请求头中没有token");
            //判断有没有秘钥
        } else if (EmptyUtil.empty(sec)) {
            return R.error().put("code", CodeConstant.USER_ERROR).put("msg", "无效的token");
        } else {
            boolean expiration = isExpiration(token, sec);
            if (expiration) {
                return R.error().put("msg", "token已经过期了").put("code", CodeConstant.UN_LOGIN);
            } else {
                //从token中获取用户名和角色
                String email = getEmail(token, sec);
                String role = getUserRole(token, sec) + "";
                String userId = getUserId(token, sec);
                String snakeName = getUserSnakeName(token, sec);
                //将角色转换成List
                List<SimpleGrantedAuthority> realRoles = new ArrayList();
                realRoles.add(new SimpleGrantedAuthority(role));
                return R.ok().put(JwtProperties.USER_EMAIL_COL_NAME, email)
                        .put(JwtProperties.USER_ROLE_COL_NAME, realRoles)
                        .put(JwtProperties.USER_ID, userId)
                        .put(JwtProperties.USER_SNAKE_NAME, snakeName);
            }
        }
    }

    @Override
    public Boolean isValid(String token) {
        return !EmptyUtil.empty(getSecByToken(token));
    }

    @Override
    public String getUserSnakeName(String token, String secretKet) {
        token = getRealToken(token);
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_SNAKE_NAME).toString();
    }

    @Override
    public String getUserSnakeName(String token) {
        String sec = (String) redisTemplate.opsForValue().get(token);
        return getUserSnakeName(token, sec);
    }

    @Override
    public String getUserId(String token, String secretKet) {
        token = getRealToken(token);
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_ID).toString();
    }

    @Override
    public String getUserId(String token) {
        return getUserId(token, getSecByToken(token)) == null ? "" : getUserId(token, getSecByToken(token));
    }

    @Override
    public Object getUserRole(String token, String secretKet) {
        token = getRealToken(token);
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_ROLE_COL_NAME);
    }

    @Override
    public Object getUserRole(String token) {
        return getUserRole(token, getSecByToken(token));
    }

    @Override
    public String getEmail(String token, String secretKet) {
        token = getRealToken(token);
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_EMAIL_COL_NAME).toString();
    }

    @Override
    public String getEmail(String token) {
        return getEmail(token, getSecByToken(token));
    }

    @Override
    public boolean isExpiration(String token, String sec) {
        token = getRealToken(token);
        Claims claims = Jwts.parser().setSigningKey(sec).parseClaimsJws(token).getBody();
        return claims.getExpiration().before(new Date());
    }

    @Override
    public boolean isExpiration(String token) {
        return isExpiration(token, getSecByToken(token));
    }

    @Override
    public String getRealToken(String token) {
        return token.split("\\|-_-\\|")[0].replace(JwtProperties.USER_TOKEN_PREFIX, "");
    }


    private String getSecByToken(String token) {
        return (String) redisTemplate.opsForValue().get(token);
    }
}
