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
@TableName("black_user")
public class BlackUserEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 用户ID
	 */
	private String userId;
	/**
	 * 被用户拉黑的用户ID
	 */
	private String blackUserId;

}
