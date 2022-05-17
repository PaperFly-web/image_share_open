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
@TableName("view_details")
public class ViewDetailsEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 浏览用户
	 */
	private String userId;
	/**
	 * 浏览的帖子
	 */
	private String postId;
	/**
	 * 浏览来源（0：推荐；1：热门；2:地点；3：话题；4：搜索；5：其他）
	 */
	private Integer viewFrom;

	/**
	 * 帖子拥有者的ID
	 */
	private String postUserId;

	/**
	 * 帖子处理后的内容
	 */
	private String postContent;

}
