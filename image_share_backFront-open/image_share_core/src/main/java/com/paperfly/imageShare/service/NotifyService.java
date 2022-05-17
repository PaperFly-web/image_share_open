package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.*;
import com.paperfly.imageShare.vo.NotifyTipsVO;

import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface NotifyService extends IService<NotifyEntity> {
    //添加消息通知,并通知
    R addAndNotify(NotifyEntity notify);

    /**
     * 添加关注消息通知
     */
    R addFocusAndNotify(FocusUserEntity focusUser);

    R addPostCommentAndNotify(PostCommentEntity postComment);

    R addThumbAndNotify(ThumbEntity thumb);
    /**
     * 保存私信消息通知
     * @param personalMessageEntity  要保存的私信信息
     * @return
     */
    R addPersonalMessageAndNotify(PersonalMessageEntity personalMessageEntity);


    /**
     * 给在线用户通知简短消息
     * @param notifyTipsVO
     * @return
     */
    R notify(NotifyTipsVO notifyTipsVO);

    /**
     * 给在线用户通知简短消息
     * @param userId
     * @return
     */
    R notify(String userId);

    /**
     * 设置接收   私信的通知消息全部已读
     * @param userId
     * @return
     */
    R personalMessageHaveRead(String userId);

    /**
     * 设置接收 当前用户  系统消息的通知消息全部已读
     * @param userId 已读用户,为空时为当前用户
     * @return
     */
    R systemMessageHaveRead(String userId);

    /**
     * 点赞全部已读
     * @param userId 已读用户,为空时为当前用户
     * @return
     */
    R thumbHaveReadAndNotify(String userId);

    /**
     * 评论全部已读
     * @param userId 已读用户,为空时为当前用户
     * @return
     */
    R postCommentHaveReadAndNotify(String userId);

    /**
     * 关注全部已读
     * @param userId 已读用户,为空时为当前用户
     * @return
     */
    R focusHaveReadAndNotify(String userId);

    R categoryCount();

    R focusDetails(Page<NotifyEntity> page);

    R postCommentDetails(Page<NotifyEntity> page);

    R thumbNotifyDetails(Page<NotifyEntity> page);

    R systemMessageNotifyDetails(Page<NotifyEntity> page);

    /**
     * 通过ID删除消息通知
     * @param id
     * @return
     */
    R deleteNotifyById(String id);
}

