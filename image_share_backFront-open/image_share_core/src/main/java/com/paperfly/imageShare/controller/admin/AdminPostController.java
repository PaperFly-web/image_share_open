package com.paperfly.imageShare.controller.admin;

import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.PostService;
import com.paperfly.imageShare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/post")
public class AdminPostController {

    @Autowired
    PostService postService;

    /**
     * 批量获取系统帖子
     * @param searchDTO
     * @return
     */
    @PostMapping("/getPosts")
    public R getPosts(@RequestBody PageSearchDTO<PostEntity> searchDTO){
        return postService.getPosts(searchDTO);
    }

    /**
     * 查询帖子
     * @param keyword
     * @return
     */
    @PostMapping("/findPostByUserSnakeNameOrEmail/{keyword}")
    public R findPostByUserSnakeNameOrEmail(@PathVariable String keyword,@RequestBody PageSearchDTO<PostEntity> searchDTO){
        return postService.findPostByUserSnakeNameOrEmail(keyword,searchDTO);
    }

    /**
     * 封杀帖子
     * @param userIds
     * @return
     */
    @PutMapping("/forbidPosts")
    public R forbidPosts(@RequestBody List<String> userIds){
        return postService.forbidPosts(userIds);
    }

    /**
     * 解开被封杀的帖子
     * @param userIds
     * @return
     */
    @PutMapping("/unmakePosts")
    public R unmakePosts(@RequestBody List<String> userIds){
        return postService.unmakePosts(userIds);
    }
}
