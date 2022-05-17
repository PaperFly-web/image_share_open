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
@TableName("notify")
public class NotifyEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 消息内容(作为提醒的内容)
	 */
	private String content;
	/**
	 * 消息类型(公告announce、提醒remind、私信message)
	 */
	private String type;
	/**
	 * 目标的ID(比如帖子ID，评论ID,一个用户ID，多个用户就为空)
	 */
	private String targetId;
	/**
	 * 目标的类型(比如文章article)
	 */
	private String targetType;
	/**
	 * 动作类型(比如点赞like)
	 */
	private String action;
	/**
	 * 发送者ID
	 */
	private String senderId;
	/**
	 * 发送者类型(前台用户user,管理员admin，系统system)
	 */
	private String senderType;
	/**
	 * 阅读状态(0:未读；1：已读)
	 */
	private Integer isRead;
	/**
	 * 消息的所属者(比如文章的作者)
	 */
	private String userId;

}
