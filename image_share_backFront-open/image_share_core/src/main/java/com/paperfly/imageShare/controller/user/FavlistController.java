package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.FavlistEntity;
import com.paperfly.imageShare.service.FavlistService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("favlist")
public class FavlistController {
    @Autowired
    private FavlistService favlistService;

    /**
     * 查询当前用户是否收藏了指定的帖子
     */
    @GetMapping("/currUserIsFavPost/{postId}")
    @OperLog(operModule = "收藏",operDesc = "查询当前用户是否收藏了指定的帖子",operType = OperTypeConst.SELECT)
    public R currUserIsFavPost(@PathVariable String postId){
        return favlistService.currUserIsFavPost(postId);
    }


    /**
     * 分页获取当前用户收藏的帖子
     * favId:收藏夹ID
     */
    @PostMapping("/{favId}")
    @OperLog(operModule = "收藏",operDesc = "分页获取当前用户收藏的帖子",operType = OperTypeConst.SELECT)
    public R getCurrUserFavlistPage(@RequestBody Page<FavlistEntity> page,@PathVariable("favId")String favId){
		return favlistService.getCurrUserFavlistPage(page,favId);
    }

    /**
     * 收藏
     */
    @PostMapping
    @OperLog(operModule = "收藏",operDesc = "添加收藏",operType = OperTypeConst.ADD)
    public R add(@RequestBody FavlistEntity favlist){
		return favlistService.add(favlist);
    }


    /**
     * 取消收藏
     */
    @DeleteMapping
    @OperLog(operModule = "收藏",operDesc = "取消收藏",operType = OperTypeConst.DELETE)
    public R cancel(@RequestBody FavlistEntity cancel){
		return favlistService.cancel(cancel);
    }

}
