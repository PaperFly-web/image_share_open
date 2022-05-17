package com.paperfly.imageShare.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.FavoriteEntity;
import com.paperfly.imageShare.service.FavoriteService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    /**
     * 获取当前用户的收藏夹列表
     */
    @PostMapping("/currUserFavorite")
    @OperLog(operModule = "收藏夹",operDesc = "获取当前用户的收藏夹列表",operType = OperTypeConst.SELECT)
    public R getCurrUserFavorite(Page<FavoriteEntity> page){
        return favoriteService.getCurrUserFavorite(page);
    }



    /**
     * 添加收藏夹
     */
    @PostMapping
    @OperLog(operModule = "收藏夹",operDesc = "添加收藏夹",operType = OperTypeConst.ADD)
    public R add(@RequestBody FavoriteEntity favorite){
		return favoriteService.add(favorite);
    }


    /**
     * 删除收藏夹
     */
    @DeleteMapping
    @OperLog(operModule = "收藏夹",operDesc = "删除收藏夹",operType = OperTypeConst.DELETE)
    public R cancel(@RequestBody FavoriteEntity favorite){
		return favoriteService.cancel(favorite);
    }

}
