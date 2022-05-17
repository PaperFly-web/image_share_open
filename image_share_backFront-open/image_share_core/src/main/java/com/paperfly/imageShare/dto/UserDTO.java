package com.paperfly.imageShare.dto;

import com.paperfly.imageShare.common.entity.BaseEntity;
import lombok.Data;

import java.util.Date;

@Data
public class UserDTO extends BaseEntity {
    /**
     * 用户状态0:正常；1：禁止登录
     */
    private Integer state;
    /**
     * 用户昵称
     */
    private String snakeName;
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 个性签名
     */
    private String signature;
    /**
     * 性别,0:女，1：男，2：不透露
     */
    private Integer sex;
    /**
     * 用户角色0:普通用户，1：管理员，2：超级管理员
     */
    private Integer role;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 用户最近一次登录时间
     */
    private Date loginTime;
    /**
     * 用户头像
     */
    private String headImage;
    /**
     * 0:正常，1：用户注销
     */
    private Integer isDeleted;
    /**
     * 验证码
     */
    private String emailCaptcha;

}
