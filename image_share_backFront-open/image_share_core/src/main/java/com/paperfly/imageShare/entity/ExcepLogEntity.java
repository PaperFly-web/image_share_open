package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.paperfly.imageShare.common.entity.BaseEntity;
import lombok.Data;


/**
 *
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-02-01 14:21:07
 */
@Data
@TableName("excep_log")
public class ExcepLogEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /**
     * 请求参数
     */
    private String excRequParam;
    /**
     * 异常响应数据
     */
    private String excResqParam;
    /**
     * 发生异常的方法
     */
    private String operMethod;
    /**
     * 发生的异常名字
     */
    private String excName;
    /**
     * 发生异常报错的信息
     */
    private String excMessage;
    /**
     * 哪个用户操作导致的异常
     */
    private String userEmail;
    /**
     * 操作异常时的URI
     */
    private String operUri;
    /**
     * 发生异常时的IP地址
     */
        private String operIp;
    /**
     * 当前应用版本号
     */
    private String operVer;
    /**
     * 请求方法类型 GET,POST,PUT,DELETE
     */
    private String reqMethodType;
}
