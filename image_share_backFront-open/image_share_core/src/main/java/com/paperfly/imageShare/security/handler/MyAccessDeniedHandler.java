package com.paperfly.imageShare.security.handler;

import cn.hutool.json.JSONUtil;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.utils.R;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
*@desc:没有权限处理器,可以正常登陆，但是当前用户访问了没有权限的资源
*@param:* @param null:
*@return:* @return: null
*@author:paperfly
*@time:2020/8/24 20:56
*/
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        response.getWriter().print(JSONUtil.toJsonStr(R.error("小弟弟,你的权限不足啊")
                .put("code", CodeConstant.UN_PERMISSIONS)
                .put("reason",accessDeniedException.getMessage())));
    }
}
