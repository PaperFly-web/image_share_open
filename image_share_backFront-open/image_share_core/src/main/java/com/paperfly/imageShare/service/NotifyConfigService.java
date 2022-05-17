package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.NotifyConfigEntity;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface NotifyConfigService extends IService<NotifyConfigEntity> {

    /**
     * 查询用户的消息配置.
     * @param userId
     * @return
     */
    NotifyConfigEntity getUserNotifyConfig(String userId);

    /**
     * 更新消息配置
     * @param notifyConfig
     * @return
     */
    R updateNotifyConfig(NotifyConfigEntity notifyConfig);

    /**
     * 获取当前用户的消息配置
     * @return
     */
    R getCurrUserNotifyConfig();
}

