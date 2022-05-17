package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.ThumbEntity;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface ThumbService extends IService<ThumbEntity> {

    /**
     * 取消点赞
     * @param thumb
     * @return
     */
    R cancel(ThumbEntity thumb);

    /**
     * 添加点赞
     * @param thumb
     * @return
     */
    R add(ThumbEntity thumb);

    /**
     * 获取帖子或者评论点赞数量
     * @param toId:被点赞帖子或评论ID
     * @param type:点赞类别：0：帖子点赞；1：评论点赞
     * @return
     */
    R getCountByType(String toId, Integer type);

    /**
     * 查询当前用户是否点击过  指定帖子/评论
     * @param toId 帖子/评论ID
     * @param type 点赞类别：0：帖子点赞；1：评论点赞
     * @return
     */
    R currUserIsThumb(String toId, Integer type);
}

