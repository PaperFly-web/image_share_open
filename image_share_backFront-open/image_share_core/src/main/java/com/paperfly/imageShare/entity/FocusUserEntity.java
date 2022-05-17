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
@TableName("focus_user")
public class FocusUserEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 用户1
	 */
	private String userIdOne;
	/**
	 * 用户2
	 */
	private String userIdTwo;
	/**
	 * 关注类别1:ID1->ID2;2:ID2->ID1;3:ID1<->ID2
	 */
	private Integer type;
	/**
	 * 用户one关注用户two时间
	 */
	private Date focusTimeOne;
	/**
	 * 用户two关注用户one时间
	 */
	private Date focusTimeTwo;

}
