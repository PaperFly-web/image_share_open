package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.FocusUserEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface FocusUserService extends IService<FocusUserEntity> {

    /**
     * 获取用户关注的用户
     * @return
     */
    List<FocusUserEntity> getUserFocusUsers(String currUserId);

    /**
     * 关注用户
     * @param focusUserId 被关注的用户ID
     * @return
     */
    R focus(String focusUserId);

    /**
     * 取消关注
     * @param focusUserId
     * @return
     */
    R cancel(String focusUserId);

    /**
     * 获取指定用户的关注列表
     * @param userId
     * @return
     */
    R getUserFocusPageById(String userId, Page<FocusUserEntity> page);

    /**
     * 获取指定用户的粉丝列表
     * @param userId
     * @return
     */
    R getUserFansPageById(String userId, Page<FocusUserEntity> page);

    /**
     * 获取粉丝数量
     * @param userId
     * @return
     */
    Long getFansCount(String userId);

    /**
     * 获取关注用户数量
     * @param userId
     * @return
     */
    Long getFocusCount(String userId);

    /**
     * 判断userIdOne是否已经关注了userIdTwo
     * @param userIdOne
     * @param userIdTwo
     * @return
     */
    R userIsFocus(String userIdOne, String userIdTwo);

    /**
     * 获取userIdOne关注userIdTwo的信息
     * @param userIdOne
     * @param userIdTwo
     * @return
     */
    R getUserIdOneFocusUserIdTwo(String userIdOne, String userIdTwo);
}

