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
@TableName("personal_message")
public class PersonalMessageEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 私信人ID
	 */
	private String userId;
	/**
	 * 被私信人ID
	 */
	private String toUserId;
	/**
	 * 私信内容
	 */
	private String content;
	/**
	 * 私信类型：0：文本：1：图片：2：分享的帖子ID
	 */
	private Integer type;

	/**
	 * 是否已读 0：未读，1：已读
	 */
	private Integer isRead;
}
