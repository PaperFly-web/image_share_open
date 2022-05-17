package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.paperfly.imageShare.common.entity.BaseEntity;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Data
@TableName("user")
public class UserEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

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
	 * 性别0：女，1：男
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
	@TableLogic(value = "0",delval = "1")
	private Integer isDeleted;

}
