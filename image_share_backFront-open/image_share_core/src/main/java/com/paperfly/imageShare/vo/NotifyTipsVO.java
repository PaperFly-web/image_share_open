package com.paperfly.imageShare.vo;

import lombok.Data;

/**
 * 简单的消息提示VO
 */
@Data
public class NotifyTipsVO {
    /**
     * 被提示的用户ID
     */
    private String userId;

    /**
     * 被提示的消息总数
     */
    private Integer count;

    /**
     * 被提示的关注数量
     */
    private Integer focusCount;

    /**
     * 被提示的评论数量
     */
    private Integer postCommentCount;

    /**
     * 被提示的点赞数量
     */
    private Integer thumbCount;

    /**
     * 被提示的私信数量
     */
    private Integer personalMessageCount;

    /**
     * 被提示的系统消息数量
     */
    private Integer systemMessageCount;

    /**
     * 本次简单的消息描述
     */
    private String desc;
}
