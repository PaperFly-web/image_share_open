package com.paperfly.imageShare.security.handler;

import cn.hutool.json.JSONUtil;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.utils.R;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
//用户未登录，但是访问了需要登录认证的资源
public class NoLoginAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
             AuthenticationException e) throws IOException {


    response.setContentType("application/json;charset=utf-8");
    response.getWriter().print(JSONUtil.toJsonStr(R.error(e.getMessage())
            .put("code", CodeConstant.UN_LOGIN)));


  }
}