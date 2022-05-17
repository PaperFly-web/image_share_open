package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;
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
@TableName("post")
public class PostEntity extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 发布帖子地点(地点的原始数据)
	 */
	private String place;
	/**
	 * 帖子未处理的原内容
	 */
//	@SerializedName(value = "originalContent", alternate = {"original_content"})
	private String originalContent;
	/**
	 * 帖子处理后的内容
	 */
//	@SerializedName(value = "handleContent", alternate = {"handle_content"})
	private String handleContent;
	/**
	 * 图片path（多个path逗号分隔）
	 */
//	@SerializedName(value = "imagesPath", alternate = {"images_path"})
	private String imagesPath;
	/**
	 * 话题（多个话题空格分隔）
	 */
	private String topic;
	/**
	 * 发布帖子的用户ID
	 */
//	@SerializedName(value = "userId", alternate = {"user_id"})
	private String userId;
	/**
	 * 帖子状态0：图片未检测1：图片检测正常2：图片违规（违规图片不显示）3：帖子异常，不予显示
	 */
	private Integer state;
	/**
	 * 点赞数量
	 */
//	@SerializedName(value = "thumbCount",alternate = "thumb_count")
	private Integer thumbCount;
	/**
	 * 评论数量
	 */
	private Integer commentCount;
	/**
	 * 浏览数量
	 */
	private Integer viewCount;
	/**
	 * 收藏数量
	 */
	private Integer favoriteCount;
	/**
	 * 是否打开评论0:关闭；1打开
	 */
	private Integer isOpenComment;
	/**
	 * 国家
	 */
	private String country;
	/**
	 * 省
	 */
	private String region;
	/**
	 * 市
	 */
	private String city;
	/**
	 * 地区
	 */
	private String area;
	/**
	 * 服务商
	 */
	private String isp;
	/**
	 * 0:代表正常；1代表删除
	 */
	@TableLogic(value = "0",delval = "1")
	private Integer isDeleted;

}
