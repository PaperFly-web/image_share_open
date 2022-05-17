package com.paperfly.imageShare.service;

import com.paperfly.imageShare.common.utils.R;


/**
 * 本地的token解析等
 */
public interface MyJwtTokenService {
    /**
     * 解析token
     * @param token
     * @return
     */
    R parseToken(String token);

    /**
     * 校验token是否有效
     * @param token
     * @return
     */
    Boolean isValid(String token);

    /**
     * 获取用户昵称
     * @param token
     * @param secretKet
     * @return
     */
    String getUserSnakeName(String token, String secretKet);

    String getUserSnakeName(String token);

    /**
     * 获取用户ID
     * @param token
     * @param secretKet
     * @return
     */
    String getUserId(String token, String secretKet);

    String getUserId(String token);

    /**
     * 获取用户角色
     * @param token
     * @param secretKet
     * @return
     */
    Object getUserRole(String token, String secretKet);

    Object getUserRole(String token);

    /**
     * 获取用户邮箱
     * @param token
     * @param secretKet
     * @return
     */
    String getEmail(String token, String secretKet);

    String getEmail(String token);

    /**
     * 判断token是否过期
     * @param token
     * @param sec
     * @return
     */
    boolean isExpiration(String token, String sec);

    boolean isExpiration(String token);

    /**
     * 由于我的token是有个后缀，并不是真实的token
     * @param token
     * @return
     */
    String getRealToken(String token);
}
