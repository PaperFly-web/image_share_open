package com.paperfly.imageShare.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.entity.PostCommentEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PostCommentVO {
    /**
     *
     */
    private String id;

    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;
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
     * 子评论数量
     */
    private Integer childrenCount;

    /**
     * 评论的点赞数量
     */
    private Integer thumbCount;
    /**
     * 子评论
     */
    private Page<PostCommentEntity> childrenPostComments;

    private String userHeadImg;
}
