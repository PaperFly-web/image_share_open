package com.paperfly.imageShare.common.constant;


/**
*@desc:jwt的常用信息
*@param:* @param null:
*@return:* @return: null
*@author:paperfly
*@time:2020/8/21 12:18
*/
public interface JwtProperties {
    //存储在redis中token的前缀，用于区分其他数据
    String REDIS_TOKEN_PREFIX="userToken_";
    String USER_EMAIL_COL_NAME="email";
    String USER_PWD_COL_NAME="password";
    String USER_ROLE_COL_NAME="role";
    String USER_ID="userId";
    String USER_SNAKE_NAME="snakeName";
    /*
    生成的token前缀
     */
    String USER_TOKEN_PREFIX="Bearer ";

    /*
    存储在header中的token参数
     */
    String AUTHORIZATION = "Authorization";

}
