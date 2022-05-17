package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.dto.PostCommentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.PostCommentEntity;
import com.paperfly.imageShare.service.PostCommentService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("postcomment")
public class PostCommentController {
    @Autowired
    private PostCommentService postCommentService;

    /**
     * 根据评论ID获取评论
     */
    @GetMapping("/getPostCommentById/{id}")
    @OperLog(operModule = "帖子评论",operDesc = "根据评论ID获取评论",operType = OperTypeConst.SELECT)
    public R getPostCommentById(@PathVariable("id")String id){
        return postCommentService.getPostCommentById(id);
    }


    /**
     * 获取父级评论，并且携带3个热门子评论
     */
    @PostMapping("/getPostCommentByPostId")
    @OperLog(operModule = "帖子评论",operDesc = "获取父级评论，并且携带3个热门子评论",operType = OperTypeConst.SELECT)
    public R getPostCommentByPostId(@RequestBody PostCommentDTO postCommentDTO){
		return postCommentService.getPostCommentByPostId(postCommentDTO);
    }

    /**
     * 获取子评论
     * @param postCommentDTO
     * @return
     */
    @PostMapping("/getChildrenPostCommentByPostId")
    @OperLog(operModule = "帖子评论",operDesc = "获取子评论",operType = OperTypeConst.SELECT)
    public R getChildrenPostCommentByPostId(@RequestBody PostCommentDTO postCommentDTO){
        return postCommentService.getChildrenPostCommentByPostId(postCommentDTO);
    }
    /**
     * 添加评论
     */
    @PostMapping
    @OperLog(operModule = "帖子评论",operDesc = "添加评论",operType = OperTypeConst.ADD)
    public R add(@RequestBody PostCommentEntity postComment){
		return postCommentService.add(postComment);
    }


    /**
     * 删除评论
     */
    @DeleteMapping
    @OperLog(operModule = "帖子评论",operDesc = "删除评论",operType = OperTypeConst.DELETE)
    public R delete(@RequestBody PostCommentEntity postComment){
		return postCommentService.delete(postComment);
    }

    /**
     * 判断当前用户是否有删除某个评论的权限
     * @param postCommentId
     * @return
     */
    @GetMapping("/currUserHasDeletePermission/{postCommentId}")
    @OperLog(operModule = "帖子评论",operDesc = "判断当前用户是否有删除某个评论的权限",operType = OperTypeConst.SELECT)
    public R currUserHasDeletePermission(@PathVariable("postCommentId")String postCommentId){
        return postCommentService.currUserHasDeletePermission(postCommentId);
    }

}
