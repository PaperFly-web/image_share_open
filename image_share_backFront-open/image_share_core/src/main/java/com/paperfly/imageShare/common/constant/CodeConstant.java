package com.paperfly.imageShare.common.constant;

import org.apache.http.HttpStatus;

public interface CodeConstant {
    int OK = 0;

    int ERROR = 1;
    /**
     * 登录异常
     */
    int LOGIN_EXCEPTION = 10600;

    /*
    未找到
     */
    int NOT_FOUND = 10404;

    /*
        身份过期/失效
     */
    int UN_LOGIN = 10401;

    /*
        没有权限
     */
    int UN_PERMISSIONS = 10402;

    /*
    	已禁止
     */
    int FORBIDDEN = 10403;

    /*
    	用户的错误
     */
    int USER_ERROR = 10444;

    /*
    服务器内部错误
     */
    int INTERNAL_SERVER_ERROR = 10500;
}
