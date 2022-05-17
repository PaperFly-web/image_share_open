package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.paperfly.imageShare.common.entity.BaseEntity;
import lombok.Data;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Data
@TableName("operation_log")
public class OperationLogEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 操作说明
	 */
	private String operDesc;
	/**
	 * 操作类型（新增，删除，查询，更新）
	 */
	private String operType;

	/**
	 * 操作模块
	 */
	private String operModule;
	/**
	 * 操作URL
	 */
	private String operUri;
	/**
	 * 操作用户userEmail
	 */
	private String userEmail;
	/**
	 * 操作用户的ID
	 */
	private String userId;
	/**
	 * 当前用户操作时的IP地址
	 */
	private String operIp;

	/**
	 * 当前操作的方法
	 */
	private String operMethod;

	/**
	 * 当前的请求参数
	 */
	private String operRequParam;

	/**
	 * 当前的响应参数
	 */
	private String operRespParam;

	/**
	 * 当前应用版本号
	 */
	private String operVer;
	/**
	 * 请求方法类型 GET,POST,PUT,DELETE
	 */
	private String reqMethodType;

}
