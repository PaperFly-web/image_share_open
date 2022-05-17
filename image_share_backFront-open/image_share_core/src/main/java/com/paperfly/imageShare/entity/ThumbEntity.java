package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.*;

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
@TableName("thumb")
public class ThumbEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 点赞类别：0：帖子点赞；1：评论点赞
	 */
	private Integer thumbType;
	/**
	 * 点赞用户ID
	 */
	private String userId;
	/**
	 * 被点赞帖子或评论ID
	 */
	private String toId;

}
