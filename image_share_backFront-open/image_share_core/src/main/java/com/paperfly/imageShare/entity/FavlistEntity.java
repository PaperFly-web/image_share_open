package com.paperfly.imageShare.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.paperfly.imageShare.common.entity.BaseEntity;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import lombok.Data;

/**
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Data
@TableName("favlist")
public class FavlistEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 收藏的帖子ID
     */
    private String postId;
    /**
     * 收藏夹IID:为空代表默认收藏夹
     */
    private String favoriteId;
    /**
     * 用户ID
     */
    private String userId;


    public void setFavoriteId(String favoriteId) {
        if (EmptyUtil.empty(favoriteId)) {
            this.favoriteId = null;
        } else {
            this.favoriteId = favoriteId;
        }
    }
}
