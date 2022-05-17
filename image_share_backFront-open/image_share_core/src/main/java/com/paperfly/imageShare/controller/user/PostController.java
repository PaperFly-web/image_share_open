package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.RecommendedEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.PostService;
import com.paperfly.imageShare.common.utils.R;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("post")
public class PostController {
    @Autowired
    private PostService postService;

    /**
     * 根据帖子ID查询帖子
     */
    @GetMapping("/getPostById/{id}")
    @OperLog(operModule = "帖子",operDesc = "根据帖子ID查询帖子",operType = OperTypeConst.SELECT)
    public R getPostById(@PathVariable("id")String id) {
        return postService.getPostById(id);
    }

    /**
     * 获取当前用户的帖子
     */
    @PostMapping("/getCurrUserPost")
    @OperLog(operModule = "帖子",operDesc = "获取当前用户的帖子",operType = OperTypeConst.SELECT)
    public R getCurrUserPost(@RequestBody Page<PostEntity> page) {
        return postService.getCurrUserPost(page);
    }

    /**
     * 获取指定用户的帖子
     */
    @PostMapping("/getUserPost/{userId}")
    @OperLog(operModule = "帖子",operDesc = "获取指定用户的帖子",operType = OperTypeConst.SELECT)
    public R getUserPostById(@RequestBody Page<PostEntity> page, @PathVariable("userId") String userId) {
        return postService.getUserPostById(page, userId);
    }

    /**
     * 获取当前用户关注的用户帖子
     *
     * @param page
     * @return
     */
    @PostMapping("/getCurrUserFocusUsersPost")
    @OperLog(operModule = "帖子",operDesc = "获取当前用户关注的用户帖子",operType = OperTypeConst.SELECT)
    public R getCurrUserFocusUsersPost(@RequestBody Page<PostEntity> page) {
        return postService.getCurrUserFocusUsersPost(page);
    }


    /**
     * 根据帖子ID查询帖子
     */
    @GetMapping("/info/{id}")
    @OperLog(operModule = "帖子",operDesc = "根据帖子ID查询帖子",operType = OperTypeConst.SELECT)
    public R info(@PathVariable("id") String id) {
        return postService.getPost(id);
    }

    /**
     * 根据评论ID获取帖子信息
     */
    @GetMapping("/getPostByPostCommentId/{postCommentId}")
    @OperLog(operModule = "帖子",operDesc = "根据评论ID获取帖子信息",operType = OperTypeConst.SELECT)
    public R getPostByPostCommentId(@PathVariable("postCommentId") String postCommentId) {
        return postService.getPostByPostCommentId(postCommentId);
    }

    /**
     * 发布帖子
     */
    @PostMapping
    @OperLog(operModule = "帖子",operDesc = "发布帖子",operType = OperTypeConst.ADD)
    public R publishPost(@RequestBody PostDTO post) {
        return postService.publishPost(post);
    }

    /**
     * 关闭或者开启评论
     */
    @PutMapping("/openOrCloseComment/{postId}/{openOrClose}")
    @OperLog(operModule = "帖子",operDesc = "关闭或者开启评论",operType = OperTypeConst.UPDATE)
    public R update(@PathVariable("postId") String postId, @PathVariable("openOrClose") Integer openOrClose) {
        return postService.openOrCloseComment(postId, openOrClose);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    @OperLog(operModule = "帖子",operDesc = "根据ID删除帖子",operType = OperTypeConst.DELETE)
    public R delete(@PathVariable("id") String id) {
        return postService.deletePost(id);
    }

    @PostMapping("/uploadPostImages")
    @OperLog(operType = OperTypeConst.ADD, operDesc = "上传帖子图片", operModule = "帖子")
    public R uploadPostImages(@RequestParam("images") MultipartFile[] files) {
        return postService.uploadFiles(files);
    }


    /**
     * 获取当前用户的推荐帖子
     */
    @PostMapping("/currUserRecommendPost")
    @OperLog(operModule = "帖子",operDesc = "获取当前用户的推荐帖子",operType = OperTypeConst.SELECT)
    public R getCurrUserRecommendPostPage(@RequestBody Page<RecommendedEntity> page) {
        return postService.getCurrUserRecommendPostPage(page);
    }



    @PostMapping("/searchPost")
    @OperLog(operModule = "帖子",operDesc = "搜索帖子",operType = OperTypeConst.SELECT)
    public R searchPost(@RequestParam("keyword")String keyword,@RequestBody Page<PostDTO> page){
        return postService.searchPost(keyword, page);
    }

    @PostMapping("/getPostByTopic")
    @OperLog(operModule = "帖子",operDesc = "根据话题获取帖子",operType = OperTypeConst.SELECT)
    public R getPostByTopic(@RequestParam("topic")String topic,@RequestBody Page<PostDTO> page){
        return postService.getPostByTopic(topic, page);
    }

    @PostMapping("/getPostByPlace")
    @OperLog(operModule = "帖子",operDesc = "根据地点获取帖子",operType = OperTypeConst.SELECT)
    public R getPostByLocal(@RequestParam("place")String place,@RequestBody Page<PostDTO> page){
        return postService.getPostByLocal(place, page);
    }

    @PostMapping("/getPostByUserName")
    @OperLog(operModule = "帖子",operDesc = "根据用户名获取帖子",operType = OperTypeConst.SELECT)
    public R getPostByUserName(@RequestParam("userName")String userName,@RequestBody Page<PostDTO> page){
        return postService.getPostByUserName(userName, page);
    }
}
