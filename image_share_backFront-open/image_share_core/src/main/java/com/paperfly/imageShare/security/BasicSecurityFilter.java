package com.paperfly.imageShare.security;

import cn.hutool.json.JSONUtil;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.constant.JwtProperties;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.MyJwtTokenUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.SpringUtils;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.math.ec.ECMultiplier;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 登录成功后 走此类进行鉴权操作
 */
@Slf4j
public class BasicSecurityFilter extends BasicAuthenticationFilter {


    public BasicSecurityFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    /**
     * 在过滤之前和之后执行的事件
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final UserService userService = SpringUtils.getBean(UserService.class);
        //解析token，返回信息
        R r = MyJwtTokenUtil.parseToken(request);

        if ((Integer) r.get("code") != CodeConstant.OK) {
            //如果获取不到token和秘钥就可以放行（因为有的资源是匿名用户也可以访问的）
            if (r.get("code").equals(CodeConstant.NOT_FOUND)) {
                log.info(r.get("msg").toString());
            } else {
                //如果携带了token但是解密不出来就返回一个错误信息
                returnMsg(response, r);
                log.info(r.get("msg").toString());
                return;
            }
        } else {
            String email = (String) r.get(JwtProperties.USER_EMAIL_COL_NAME);
            String userId = (String) r.get(JwtProperties.USER_ID);
            String snakeName = (String) r.get(JwtProperties.USER_SNAKE_NAME);
            List<SimpleGrantedAuthority> roles = (List<SimpleGrantedAuthority>) r.get(JwtProperties.USER_ROLE_COL_NAME);
            final UserEntity user = new UserEntity();
            user.setId(userId);
            user.setEmail(email);
            user.setRole(Integer.valueOf(roles.get(0).toString()));
            user.setSnakeName(snakeName);
            //检查用户是否被禁止登录
            if(!EmptyUtil.empty(userId) && userService.userIsForbid(userId)){
                log.info("用户被禁止登录");
                returnMsg(response, R.error(CodeConstant.UN_LOGIN, "您被禁止登录,请联系管理员"));
                return;
            }
            if (EmptyUtil.empty(email)) {
                log.info("邮箱为空");
                returnMsg(response, R.error(CodeConstant.UN_LOGIN, "邮箱为空"));
                return;
            } else {
                try {
                    //授权检查通过
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, MyJwtTokenUtil.getTokenByHttpServletRequest(request), roles));
                } catch (Exception e) {
                    //返回json形式的错误信息
                    String reason = "统一处理，原因：" + e.getMessage();
                    returnMsg(response, R.error(CodeConstant.FORBIDDEN, reason));
                    return;
                }

            }
        }
        super.doFilterInternal(request, response, chain);
    }


    /**
     * 返回响应流给前端
     *
     * @param response
     * @param r
     */
    public void returnMsg(HttpServletResponse response, R r) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.write(JSONUtil.toJsonStr(r));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!EmptyUtil.empty(out)) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}