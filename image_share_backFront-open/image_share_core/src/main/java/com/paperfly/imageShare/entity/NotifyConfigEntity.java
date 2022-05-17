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
@TableName("notify_config")
public class NotifyConfigEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 点赞的消息配置：0：关闭，1：我关注的用户，2：代表任何用户
	 */
	private Integer thumb;
	/**
	 * 评论的消息配置：0：关闭，1：我关注的用户，2：代表任何用户
	 */
	private Integer comment;
	/**
	 * 关注消息配置：0：关闭，1：开启
	 */
	private Integer follow;
	/**
	 * 私信的消息配置：0：关闭，1：我关注的用户，2：代表任何用户
	 */
	private Integer personalMessage;
	/**
	 * 系统消息配置：0：关闭，2：开启
	 */
	private Integer systemMessage;

}
