package com.paperfly.imageShare.common.utils;

import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.constant.JwtProperties;
import com.paperfly.imageShare.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @desc:JWT的工具类
 * @param:* @param null:
 * @return:* @return: null
 * @author:paperfly
 * @time:2020/8/21 12:46
 */
public class MyJwtTokenUtil {

    public static String createToken(String sec, Integer expTime, Authentication authentication) {

        //获取用户身份
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        return createToken(sec,expTime,userEntity);
    }
    public static String createToken(String sec, Integer expTime, UserEntity userEntity) {
        //token过期时间，以天为单位
        expTime = expTime * 24 * 60 * 60 * 1000;
        Map<String, Object> header = new HashMap<>();
        String email = userEntity.getEmail();
        Integer role = userEntity.getRole();
        String snakeName = userEntity.getSnakeName();
        String issuer = "paperfly";
        Date now = new Date();
        Date expireTime = new Date(System.currentTimeMillis() + expTime);
        String userId = userEntity.getId();
        header.put("alg", "HS256");
        header.put("type", "JWT");
        return Jwts
                .builder()
                .setHeader(header)
                .claim(JwtProperties.USER_ROLE_COL_NAME, role)
                .claim(JwtProperties.USER_EMAIL_COL_NAME, email)
                .claim(JwtProperties.USER_ID, userId)
                .claim(JwtProperties.USER_SNAKE_NAME, snakeName)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS256, sec)
                //压缩
                .compact()
                //添加一个token后缀，保证存储在redis的token唯一
                + "|-_-|" + email;
    }

    /**
     * 校验Token是否过期
     */
    public static boolean isExpiration(String token, String sec) {
        Claims claims = Jwts.parser().setSigningKey(sec).parseClaimsJws(token).getBody();
        return claims.getExpiration().before(new Date());
    }


    /**
     * 从Token中获取username
     */
    public static String getEmail(String token, String secretKet) {
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_EMAIL_COL_NAME).toString();
    }

    /**
     * 从Token中获取用户角色
     */
    public static Object getUserRole(String token, String secretKet) {
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_ROLE_COL_NAME);
    }

    /**
     * 从Token中获取username
     */
    public static String getUserId(String token, String secretKet) {
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_ID).toString();
    }

    /**
     * 从Token中获取username
     */
    public static String getUserSnakeName(String token, String secretKet) {
        Claims claims = Jwts.parser().setSigningKey(secretKet).parseClaimsJws(token).getBody();
        return claims.get(JwtProperties.USER_SNAKE_NAME).toString();
    }

    /**
     * 通过请求信息，解析token
     *
     * @param request
     * @return
     */
    public static R parseToken(HttpServletRequest request) {
        RedisTemplate redisTemplate = (RedisTemplate) WebApplicationContextUtil.getBean("redisTemplate", request);
        String token = getTokenByHttpServletRequest(request);
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
                String role =  getUserRole(token, sec)+"";
                String userId = getUserId(token, sec);
                String snakeName = getUserSnakeName(token, sec);
                //将角色转换成List
                List<SimpleGrantedAuthority> realRoles = new ArrayList();
                realRoles.add(new SimpleGrantedAuthority(role));
                return R.ok().put(JwtProperties.USER_EMAIL_COL_NAME, email)
                        .put(JwtProperties.USER_ROLE_COL_NAME, realRoles)
                        .put(JwtProperties.USER_ID,userId)
                        .put(JwtProperties.USER_SNAKE_NAME,snakeName);
            }
        }
    }


    public static String getTokenByHttpServletRequest(HttpServletRequest request) {
        //获取token信息
        String token = request.getHeader(JwtProperties.AUTHORIZATION);
        return EmptyUtil.empty(token) ? "" : token;
    }

    /**
     * 检查token是否有效
     * @param token
     * @return
     */
    public static Boolean isValid(String token,HttpServletRequest request){
        RedisTemplate redisTemplate = (RedisTemplate) WebApplicationContextUtil.getBean("redisTemplate", request);
        final String sec = (String) redisTemplate.opsForValue().get(token);
        if(EmptyUtil.empty(sec)){
            return false;
        }
        return true;
    }

}
