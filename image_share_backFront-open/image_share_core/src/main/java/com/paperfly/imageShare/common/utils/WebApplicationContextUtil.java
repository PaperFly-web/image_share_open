package com.paperfly.imageShare.common.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class WebApplicationContextUtil {
    /**
     * 在filter中，提前获取bean
     * @param beanName
     * @param req
     * @return
     */
    public static Object getBean(String beanName, HttpServletRequest req){
        ServletContext sc = req.getSession().getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(sc);
        Object bean = ctx.getBean(beanName);
        return bean;
    }
}
