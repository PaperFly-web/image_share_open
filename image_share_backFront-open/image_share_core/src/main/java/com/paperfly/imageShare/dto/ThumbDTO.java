package com.paperfly.imageShare.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ThumbDTO {
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

    /**
     *
     */
    private String id;

    /**
     * 帖子或评论的点赞数量
     */
    private Integer count;

    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;
}
