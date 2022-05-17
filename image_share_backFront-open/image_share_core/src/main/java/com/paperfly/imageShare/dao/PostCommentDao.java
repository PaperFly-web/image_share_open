package com.paperfly.imageShare.dao;

import com.paperfly.imageShare.dto.PostCommentDTO;
import com.paperfly.imageShare.entity.PostCommentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface PostCommentDao extends BaseMapper<PostCommentEntity> {

    /**
     * 删除评论
     * @param postComment
     * @return
     */
    Integer delete(PostCommentEntity postComment);

    /**
     * 添加评论
     * @param postComment
     * @return
     */
    Integer add(PostCommentEntity postComment);

}
