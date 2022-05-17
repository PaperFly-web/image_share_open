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
 * @date 2022-02-04 14:47:37
 */
@Data
@TableName("temp_file")
public class TempFileEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 * 文件完整路径。文件完整路径中不能包含Bucket名称。
	 */
	private String objectName;

}
