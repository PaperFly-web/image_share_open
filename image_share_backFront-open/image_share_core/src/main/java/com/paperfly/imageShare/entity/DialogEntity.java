package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.paperfly.imageShare.common.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Data
@TableName("dialog")
public class DialogEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 创建会话的UserId
	 */
	private String userId;

	/**
	 * 和谁创建的userId
	 */
	private String toUserId;

	/**
	 * 会话类型0：私信，1：系统通知
	 */
	private Integer type;

/*	*//**
	 * 0:user_id删除会话，1：to_user_id删除会话
	 *//*
	//后续功能升级，单方面关闭会话
	private Integer deleteType;*/

}
