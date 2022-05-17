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
@TableName("report_type")
public class ReportTypeEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 举报分类父ID(为空，代表大分类)
	 */
	private String fatherReportTypeId;
	/**
	 * 举报分类内容
	 */
	private String reportTypeContent;
	/**
	 * 举报类别：0：举报用户1：举报评论2：举报帖子
	 */
	private Integer reportType;

}
