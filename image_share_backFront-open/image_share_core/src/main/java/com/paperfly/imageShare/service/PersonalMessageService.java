package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface PersonalMessageService extends IService<PersonalMessageEntity> {

    /**
     * 添加私信消息
     * @param personalMessageEntity
     * @return
     */
    R addAndUpdateDialogUpdateTime(PersonalMessageEntity personalMessageEntity);

    /**
     * 获取当前用户  与被私信用户的历史私信消息
     * @param page
     * @param toUserId
     * @return
     */
    R getCurrUserToUserPersonalMessage(Page<PersonalMessageEntity> page, String toUserId);

    /**
     * 设置对某用户消息全部已读
     * @param toUserId
     * @return
     */
    R haveRead(String toUserId);
    /**
     * 设置对某用户消息全部已读,并且更当前用户的新消息通知
     * @param toUserId
     * @return
     */
    R haveReadAndUpdateNotify(String toUserId);

    /**
     * 获取当前用户对  toUserId的未读数量
     * @param toUserId
     * @return
     */
    Integer getNoReadCount(String toUserId);
}

