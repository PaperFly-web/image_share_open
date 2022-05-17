package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.entity.RecommendedEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface PostService extends IService<PostEntity> {

    /**
     * 上传帖子的图片
     * @param files 图片
     * @return
     */
    R uploadFiles(MultipartFile[] files);

    /**
     * 发布帖子
     * @param postDTO 帖子信息
     * @return
     */
    R publishPost(PostDTO postDTO);

    /**
     * 删除帖子
     * @param id 帖子ID
     * @return
     */
    R deletePost(String id);

    /**
     * 获取帖子
     * @param id 帖子ID
     * @return
     */
    R getPost(String id);

    /**
     * 批量获取POST
     * @param ids post的id
     * @return
     */
    R getPost(List<String> ids);

    /**
     * 查询当前用户的帖子
     * @param page
     * @return
     */
    R getCurrUserPost(Page<PostEntity> page);

    /**
     * 获取当前用户关注的用户帖子
     * @param page
     * @return
     */
    R getCurrUserFocusUsersPost(Page<PostEntity> page);

    /**
     * 获取指定用户ID的帖子
     * @param page
     * @param userId
     * @return
     */
    R getUserPostById(Page<PostEntity> page, String userId);

    /**
     * 开启或关闭评论
     * @param postId
     * @param openOrClose
     * @return
     */
    R openOrCloseComment(String postId, Integer openOrClose);

    /**
     * 1.由于数据库中的图片和话题，都是字符串方式保存的，所以要把字符串方式转换成List集合
     * 2.因为图片是私有的，所以还要获取到图片的临时访问URL
     *
     * @param postEntity
     * @return
     */
    PostDTO postEntityToPostDTO(PostEntity postEntity);

    /**
     * 获取当前用户的推荐帖子
     */
    R getCurrUserRecommendPostPage(Page<RecommendedEntity> page);


    /**
     * 帖子转换：
     * 1.由于数据库中的图片和话题，都是字符串方式保存的，所以要把字符串方式转换成List集合
     * 2.因为图片是私有的，所以还要获取到图片的临时访问URL
     * @param result
     * @return
     */
    Page<PostDTO> postEntityPageToPostDTOPage(IPage<PostEntity> result);

    /**
     * 根据评论ID获取帖子信息
     * @param postCommentId
     * @return
     */
    R getPostByPostCommentId(String postCommentId);

    /**
     * 搜索帖子
     * @param keyword 关键词
     * @return
     */
    R searchPost(String keyword,Page<PostDTO> page);

    /**
     * 根据话题获取帖子
     * @param topic
     * @param page
     * @return
     */
    R getPostByTopic(String topic, Page<PostDTO> page);

    /**
     * 根据地点获取帖子
     * @param place
     * @param page
     * @return
     */
    R getPostByLocal(String place, Page<PostDTO> page);

    /**
     * 根据帖子ID获取帖子
     * @param id
     * @return
     */
    R getPostById(String id);

    /**
     * 管理员批量获取用户帖子
     * @param searchDTO
     * @return
     */
    R getPosts(PageSearchDTO<PostEntity> searchDTO);

    /**
     * 管理员根据用户邮箱或昵称搜索帖子
     * @param keyWord
     * @param searchDTO
     * @return
     */
    R findPostByUserSnakeNameOrEmail(String keyWord, PageSearchDTO<PostEntity> searchDTO);

    /**
     * 管理员封杀帖子
     * @param userIds
     * @return
     */
    R forbidPosts(List<String> userIds);

    /**
     * 管理员解开被封杀的帖子
     * @param userIds
     * @return
     */
    R unmakePosts(List<String> userIds);

    /**
     * 根据用户名获取帖子
     * @param userName
     * @param page
     * @return
     */
    R getPostByUserName(String userName, Page<PostDTO> page);
}

