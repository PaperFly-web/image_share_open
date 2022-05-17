package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.FavlistEntity;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface FavlistService extends IService<FavlistEntity> {

    /**
     * 用户添加收藏
     * @param favlist
     * @return
     */
    R add(FavlistEntity favlist);

    /**
     * 取消收藏
     * @param cancel
     * @return
     */
    R cancel(FavlistEntity cancel);


    /**
     * 查询当前用户是否收藏了指定的帖子
     * @param postId
     * @return
     */
    R currUserIsFavPost(String postId);

    /**
     * 分页获取当前用户收藏的帖子
     * @param page
     * @param favId 收藏夹ID/传入null时代表为默认收藏夹
     * @return
     */
    R getCurrUserFavlistPage(Page<FavlistEntity> page,String favId);
}

