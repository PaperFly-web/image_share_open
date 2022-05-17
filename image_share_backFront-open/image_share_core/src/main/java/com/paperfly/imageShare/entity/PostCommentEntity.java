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
@TableName("post_comment")
public class PostCommentEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 评论父ID（为空代表为父）
	 */
	private String fatherCommentId;
	/**
	 * 回复用户名称
	 */
	private String replayUserSnakeName;
	/**
	 * 评论未处理时内容
	 */
	private String originalContent;
	/**
	 * 评论处理后的内容
	 */
	private String handleContent;
	/**
	 * 评论用户昵称
	 */
	private String snakeName;
	/**
	 * 评论用户ID
	 */
	private String userId;
	/**
	 * 评论的帖子ID
	 */
	private String postId;
	/**
	 * 评论状态0：未读1：已读2：违规
	 */
	private Integer state;

	/**
	 * 评论的点赞数量
	 */
	private Integer thumbCount;
}
