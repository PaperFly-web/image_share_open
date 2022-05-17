package com.paperfly.imageShare.security.handler;

import cn.hutool.json.JSONUtil;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.constant.JwtProperties;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.MyJwtTokenUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.WebApplicationContextUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 处理退出登录的处理器
 */
public class MyLogoutHandler implements LogoutSuccessHandler {



    @Override
    public void onLogoutSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authentication) throws IOException, ServletException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();


        //在filter中提前获取bean
        RedisTemplate redisTemplate = (RedisTemplate) WebApplicationContextUtil.getBean("redisTemplate", req);
        //获取token
        String token = MyJwtTokenUtil.getTokenByHttpServletRequest(req);
        String sec = (String)redisTemplate.opsForValue().get(token);
        //获取到秘钥就可以放行
        if(!EmptyUtil.empty(sec)){
            //把redis的token删除掉
            redisTemplate.delete(token);
            out.write(JSONUtil.toJsonStr(R.ok("退出登录成功")) );
        }else {
            out.write(JSONUtil.toJsonStr(R.error(CodeConstant.UN_LOGIN,"传递的token无效")) );
        }

        out.flush();
        out.close();
    }
}
