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
@TableName("recommended")
public class RecommendedEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 推荐用户ID
	 */
	private String userId;
	/**
	 * 推荐给用户的帖子ID
	 */
	private String postId;
	/**
	 * 推荐类型（0：userCF;1:itemCF;2:热门推荐；3：地点推荐；4：话题推荐；5：其他）
	 */
	private Integer type;
	/**
	 * 推荐评分
	 */
	private Float score;
}
