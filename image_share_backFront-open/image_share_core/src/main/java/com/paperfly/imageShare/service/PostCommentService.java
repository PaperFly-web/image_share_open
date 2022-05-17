package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PostCommentDTO;
import com.paperfly.imageShare.entity.PostCommentEntity;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface PostCommentService extends IService<PostCommentEntity> {

    /**
     * 添加评论
     * @param postComment
     * @return
     */
    R add(PostCommentEntity postComment);

    /**
     * 删除评论
     * @param postComment 要删除的评论数据
     * @return
     */
    R delete(PostCommentEntity postComment);

    R getPostCommentByPostId(PostCommentDTO postCommentDTO);
    /**
     * 获取帖子的顶级评论
     * @param
     * @return
     */
    R getRootPostCommentByPostId(PostCommentDTO postCommentDTO);

    /**
     * 获取帖子的顶级评论的子评论
     * @param
     * @return
     */
    R getChildrenPostCommentByPostId(PostCommentDTO postCommentDTO);

    /**
     * 判断当前用户是否有删除某个评论的权限
     * @param postCommentId 评论ID
     * @return
     */
    R currUserHasDeletePermission(String postCommentId);

    /**
     * 根据评论ID获取评论
     * @param id
     * @return
     */
    R getPostCommentById(String id);
}

