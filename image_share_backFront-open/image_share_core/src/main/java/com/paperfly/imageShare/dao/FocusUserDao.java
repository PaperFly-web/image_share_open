package com.paperfly.imageShare.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.entity.FocusUserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Mapper
public interface FocusUserDao extends BaseMapper<FocusUserEntity> {
    /**
     * 获取用户关注的用户
     * @return
     */
    List<FocusUserEntity> getUserFocusUsers(String userId);

    /**
     * 分页获取用户关注的用户
     * @return
     */
    IPage<FocusUserEntity> getUserFocusUsersPage(Page<FocusUserEntity> page,
                                                  String userId);
    /**
     * 分页获取用户关注的用户
     * @return
     */
    IPage<FocusUserEntity> getUserFansUsersPage(Page<FocusUserEntity> page,
                                                 String userId);

    Long getFocusCount(String userId);

    Long getFansCount(String userId);

    /**
     * 获取userIdOne关注userIdTwo的关注信息
     * @param focusUserEntity
     * @return
     */
    FocusUserEntity getUserFocusUser(FocusUserEntity focusUserEntity);
}
