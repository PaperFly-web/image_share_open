package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.DialogDTO;
import com.paperfly.imageShare.entity.DialogEntity;
import com.paperfly.imageShare.entity.NotifyEntity;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import com.paperfly.imageShare.vo.NotifyTipsVO;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface DialogService extends IService<DialogEntity> {

    /**
     * 创建会话
     * @param dialogDTO
     * @return
     */
    R create(DialogDTO dialogDTO);

    /**
     * 如果user与toUser双会话都没创建，则创建会话  user与toUser会话
     * 使用的redis缓存，所以性能可以保证
     * @param dialogDTO
     * @return
     */
    R createUserAndToUserDialog(DialogDTO dialogDTO);

    /**
     * 判断是否已经创建过会话
     * @param dialogDTO
     * @return
     */
    Boolean isCreate(DialogDTO dialogDTO);

    /**
     * 查询当前用户的会话
     * @param page
     * @return
     */
    R getCurrUserDialogPage(Page<DialogEntity> page);

    /**
     * 删除会话
     * @param toUserId 对话用户
     * @return
     */
    R delete(String toUserId);

    /**
     * 更新userId与toUserId的对话时间
     * @param userId
     * @param toUserId
     * @return
     */
    R updateDoubleDialogUpdateTime(String userId,String toUserId);

    /**
     * 获取和指定用户的会话
     * @param toUserId
     * @return
     */
    R getDialogByToUserId(String toUserId);
}

