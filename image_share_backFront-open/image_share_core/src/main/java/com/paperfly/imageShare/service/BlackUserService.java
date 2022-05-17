package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.BlackUserEntity;
import io.swagger.models.auth.In;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface BlackUserService extends IService<BlackUserEntity> {

    /**
     * 查询当前用户黑名单
     * @param page
     * @return
     */
    R getCurrUserBlackUser(Page<BlackUserEntity> page);

    /**
     * 当前用户移除黑名单
     * @param blackUserId
     * @return
     */
    R deleteCurrUserBlackUser(String blackUserId);

    /**
     * 添加黑名单
     * @param blackUserId
     * @return
     */
    R addCurrBlackUser(String blackUserId);

    /**
     * 获取当前用户的黑名单IDS
     * @return
     */
    Set<String> getCurrUserBlackUserIds();

    /**
     * 获取指定用户的黑名单IDS
     * @return
     */
    Set<String> getSpecifyUserBlackUserIds(String userId);

    /**
     * 判断是否为当前用户的黑名单
     * @param userId
     * @return
     */
    Boolean isBlackUserMember(String userId);

    /**
     * 判断blackUserId是否为userId的黑名单用户
     * @param userId
     * @param blackUserId
     * @return
     */
    Boolean isBlackUserMember(String userId,String blackUserId);

    /**
     * 获取指定用户的黑名单数量
     * @param userId  为空时候，获取当前用户黑名单数量
     * @return
     */
    Long getUserBlackUserCount(String userId);
}

