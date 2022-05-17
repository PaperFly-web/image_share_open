package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.ListUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.common.utils.baidu.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.NotifyConfigDao;
import com.paperfly.imageShare.entity.NotifyConfigEntity;
import com.paperfly.imageShare.service.NotifyConfigService;
import org.springframework.transaction.annotation.Transactional;


@Service("notifyConfigService")
@Transactional
public class NotifyConfigServiceImpl extends ServiceImpl<NotifyConfigDao, NotifyConfigEntity> implements NotifyConfigService {

    @Autowired
    NotifyConfigDao notifyConfigDao;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public static final String REDIS_NOTIFY_CONFIG_PREFIX = "NOTIFY_CONFIG_";

    @Override
    public NotifyConfigEntity getUserNotifyConfig(String userId) {
        if (EmptyUtil.empty(userId)) {
            return null;
        }
        //1.先查询redis中有没有
        final String redisNotifyConfig = redisTemplate.opsForValue().get(REDIS_NOTIFY_CONFIG_PREFIX + userId);
        if (!EmptyUtil.empty(redisNotifyConfig)) {
            return GsonUtils.fromJson(redisNotifyConfig, NotifyConfigEntity.class);
        }
        final QueryWrapper<NotifyConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        final NotifyConfigEntity res = notifyConfigDao.selectOne(queryWrapper);
        if (EmptyUtil.empty(res)) {
            return getDefaultNotifyConfigEntity(userId);
        }
        return res;
    }

    @Override
    public R updateNotifyConfig(NotifyConfigEntity notifyConfig) {
        if (EmptyUtil.empty(notifyConfig)) {
            return R.userError("实体类为空");
        }
        //1.检查各个消息配置数据是否正确
        //检查点赞
        if (EmptyUtil.empty(notifyConfig.getThumb()) || !ListUtil.contains(notifyConfig.getThumb(), 0, 1, 2)) {
            return R.userError("点赞消息配置数据错误");
        }
        //检查评论
        if (EmptyUtil.empty(notifyConfig.getComment()) || !ListUtil.contains(notifyConfig.getComment(), 0, 1, 2)) {
            return R.userError("评论消息配置数据错误");
        }
        //检查关注
        if (EmptyUtil.empty(notifyConfig.getFollow()) || !ListUtil.contains(notifyConfig.getFollow(), 0, 1, 2)) {
            return R.userError("关注消息配置数据错误");
        }
        //检查私信
        if (EmptyUtil.empty(notifyConfig.getPersonalMessage()) || !ListUtil.contains(notifyConfig.getPersonalMessage(), 0, 1, 2)) {
            return R.userError("私信消息配置数据错误");
        }
        notifyConfig.setUserId(UserSecurityUtil.getCurrUserId());
        notifyConfig.setCreateTime(new Date());
        notifyConfig.setUpdateTime(new Date());

        final UpdateWrapper<NotifyConfigEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", notifyConfig.getUserId());
        updateWrapper.set("thumb", notifyConfig.getThumb());
        updateWrapper.set("comment", notifyConfig.getComment());
        updateWrapper.set("follow", notifyConfig.getFollow());
        updateWrapper.set("personal_message", notifyConfig.getPersonalMessage());
        updateWrapper.set("update_time", new Date());
        //检查当前是否已经有过配置
        if (isDefaultConfig(UserSecurityUtil.getCurrUserId())) {
            final int insertCount = notifyConfigDao.insert(notifyConfig);
            if (insertCount > 0) {
                redisTemplate.opsForValue().set(REDIS_NOTIFY_CONFIG_PREFIX + UserSecurityUtil.getCurrUserId(), GsonUtils.toJson(notifyConfig));
                return R.ok("消息配置修改成功");
            }
        }else {
            final int updateCount = notifyConfigDao.update(null, updateWrapper);
            if(updateCount>0){
                //获取消息配置的创建时间
                final String redisJsoNotifyConfig = redisTemplate.opsForValue().get(REDIS_NOTIFY_CONFIG_PREFIX + UserSecurityUtil.getCurrUserId());
                final Date createTime = GsonUtils.fromJson(redisJsoNotifyConfig, NotifyConfigEntity.class).getCreateTime();
                notifyConfig.setCreateTime(createTime);
                redisTemplate.opsForValue().set(REDIS_NOTIFY_CONFIG_PREFIX + UserSecurityUtil.getCurrUserId(), GsonUtils.toJson(notifyConfig));
                return R.ok("消息配置修改成功");
            }
        }

        return R.error("不知道为何，修改消息配置失败");
    }

    @Override
    public R getCurrUserNotifyConfig() {
        final NotifyConfigEntity userNotifyConfig = this.getUserNotifyConfig(UserSecurityUtil.getCurrUserId());
        return R.ok("查询成功").put("data",userNotifyConfig);
    }

    /**
     * 获取默认消息配置
     *
     * @return
     */
    private NotifyConfigEntity getDefaultNotifyConfigEntity(String userId) {
        final NotifyConfigEntity notifyConfigEntity = new NotifyConfigEntity();
        notifyConfigEntity.setUserId(userId);
        notifyConfigEntity.setComment(2);
        notifyConfigEntity.setThumb(2);
        notifyConfigEntity.setPersonalMessage(2);
        notifyConfigEntity.setFollow(2);
        notifyConfigEntity.setSystemMessage(2);
        return notifyConfigEntity;
    }

    //判断是不是默认配置，如果是默认配置那么redis在之前没有缓存
    private boolean isDefaultConfig(String userId) {
        if (EmptyUtil.empty(userId)) {
            return true;
        }
        return !redisTemplate.hasKey(REDIS_NOTIFY_CONFIG_PREFIX + userId);
    }


}