package com.paperfly.imageShare.dto;

import lombok.Data;


/**
 * 可以直接修改的用户信息DTO
 */
@Data
public class UserInfoDTO {
    private String id;
    /**
     * 个性签名
     */
    private String signature;
    /**
     * 性别0：女，1：男
     */
    private Integer sex;
    /**
     * 用户名
     */
    private String username;
}
