package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.FavoriteEntity;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface FavoriteService extends IService<FavoriteEntity> {

    /**
     * 获取当前用户收藏夹
     * @param page
     * @return
     */
    R getCurrUserFavorite(Page<FavoriteEntity> page);

    /**
     * 当前用户添加收藏夹
     * @param favorite
     * @return
     */
    R add(FavoriteEntity favorite);

    /**
     * 删除当前用户的收藏夹
     * @param favorite
     * @return
     */
    R cancel(FavoriteEntity favorite);
}

