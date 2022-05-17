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
@TableName("report")
public class ReportEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 处理这个举报管理员的ID
	 */
	private String managerId;
	/**
	 * 审核这个举报时间
	 */
	private Date auditTime;
	/**
	 * 举报分类ID
	 */
	private String reportTypeId;
	/**
	 * 举报人ID
	 */
	private String userId;
	/**
	 * 被举报ID(用户，帖子，评论)
	 */
	private String reportId;
	/**
	 * 举报状态0：未处理，1：已处理
	 */
	private Integer state;
	/**
	 * 举报内容
	 */
	private String reportContent;
	/**
	 * 举报类别：0：举报用户1：举报评论2：举报帖子
	 */
	private Integer reportType;

	/**
	 * 是否违规 0违规，1未违规
	 */
	private Integer isIll;

}
