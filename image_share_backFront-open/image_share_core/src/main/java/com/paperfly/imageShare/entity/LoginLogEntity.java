package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.paperfly.imageShare.common.entity.BaseEntity;
import lombok.Data;

/**
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Data
@TableName("login_log")
public class LoginLogEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户Email
     */
    private String userEmail;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 登录结果登录结果
     * 0：登录成功；
     * 1：系统检测到您常驻城市发生改变,请进行邮箱验证码进一步验证；
     * 2：邮件验证码错误或过期；
     * 3：图片验证码错误或过期；
     * 4：用户未发现；
     * 5：用邮箱或密码错误；
     * 6：用户被锁定；
     * 45：验证码错误；
     * 99：登录失败；
     * 100：未知登录异常
     */
    private Integer result;
    /**
     * 登录结果描述
     */
    private String resultDesc;
    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 地点的原始数据
     */
    private String place;
    /**
     * 国家
     */
    private String country;
    /**
     * 区域
     */
    private String area;
    /**
     * 省份
     */
    private String region;
    /**
     * 城市
     */
    private String city;

    /**
     * 服务商
     */
    private String isp;

}
