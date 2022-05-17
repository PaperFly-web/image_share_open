package com.paperfly.imageShare.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.entity.FocusUserEntity;
import com.paperfly.imageShare.entity.PostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.paperfly.imageShare.entity.RecommendedEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Mapper
public interface PostDao extends BaseMapper<PostEntity> {

    IPage<PostEntity> getCurrUserRecommendPostPage(Page<RecommendedEntity> page, String userId);

    /**
     * 根据评论ID获取帖子信息
     * @param postCommentId
     * @return
     */
    PostEntity getPostByPostCommentId(String postCommentId);
}
