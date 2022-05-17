package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.dto.DialogDTO;
import com.paperfly.imageShare.entity.DialogEntity;
import com.paperfly.imageShare.service.DialogService;
import com.paperfly.imageShare.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.PersonalMessageDao;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import com.paperfly.imageShare.service.PersonalMessageService;
import org.springframework.transaction.annotation.Transactional;


@Service("personalMessageService")
@Transactional
public class PersonalMessageServiceImpl extends ServiceImpl<PersonalMessageDao, PersonalMessageEntity> implements PersonalMessageService {

    @Autowired
    PersonalMessageDao personalMessageDao;

    @Autowired
    DialogService dialogService;

    @Autowired
    NotifyService notifyService;

    @Override//不能使用UserSecurityUtil，因为发送消息没走权限验证
    public R addAndUpdateDialogUpdateTime(PersonalMessageEntity personalMessageEntity) {
        if (EmptyUtil.empty(personalMessageEntity)) {
            return R.userError("消息体为空");
        }
        //1.检查被私信的用户ID是否携带
        if (EmptyUtil.empty(personalMessageEntity.getUserId())) {
            return R.userError("私信的用户ID不能为空");
        }
        //2.检查被私信的用户ID是否携带
        if (EmptyUtil.empty(personalMessageEntity.getToUserId())) {
            return R.userError("被私信的用户ID不能为空");
        }
        //3.检查私信内容
        if (EmptyUtil.empty(personalMessageEntity.getContent().trim()) ||
                personalMessageEntity.getContent().length() > 800) {
            return R.userError("私信内容字数需要大于0小于等于800");
        }
        //暂时只支持文本消息
        personalMessageEntity.setType(0);
        //设置时间
        personalMessageEntity.setCreateTime(new Date());
        personalMessageEntity.setUpdateTime(new Date());
        //设置消息未读
        if (EmptyUtil.empty(personalMessageEntity.getIsRead()) ||
                (personalMessageEntity.getIsRead() != 0 && personalMessageEntity.getIsRead() != 1)) {
            personalMessageEntity.setIsRead(0);
        }
        final int insertCount = personalMessageDao.add(personalMessageEntity);
        if (insertCount > 0) {
            final DialogDTO dialogDTO = new DialogDTO();
            dialogDTO.setUserId(personalMessageEntity.getUserId());
            dialogDTO.setToUserId(personalMessageEntity.getToUserId());
            dialogDTO.setType(0);
            //如果user与toUser双会话都没创建，则创建会话  user与toUser会话
            //使用的redis缓存，所以性能可以保证
            dialogService.createUserAndToUserDialog(dialogDTO);
            //更新会话时间
            dialogService.updateDoubleDialogUpdateTime(personalMessageEntity.getUserId(), personalMessageEntity.getToUserId());
            return R.ok("私信消息保存成功");
        } else {
            return R.error("不知为何，私信消息保存失败");
        }
    }

    @Override
    public R getCurrUserToUserPersonalMessage(Page<PersonalMessageEntity> page, String toUserId) {
        if (EmptyUtil.empty(toUserId)) {
            return R.userError("被私信用户ID不能为空");
        }
        final PersonalMessageEntity personalMessageEntity = new PersonalMessageEntity();
        personalMessageEntity.setUserId(UserSecurityUtil.getCurrUserId());
        personalMessageEntity.setToUserId(toUserId);
        final IPage<PersonalMessageEntity> resultPage = personalMessageDao.
                getCurrUserToUserPersonalMessage(page, personalMessageEntity);
        return R.ok("查询私信消息成功").put("data", resultPage);
    }

    @Override
    public R haveRead(String toUserId) {
        if (EmptyUtil.empty(toUserId)) {
            return R.userError("对方用户ID不能为空");
        }
        final UpdateWrapper<PersonalMessageEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", toUserId);
        updateWrapper.eq("to_user_id", UserSecurityUtil.getCurrUserId());
        updateWrapper.set("is_read", 1);
//        final boolean updateCount = this.update(updateWrapper);
        final int updateCount = personalMessageDao.update(null, updateWrapper);
        notifyService.personalMessageHaveRead(toUserId);
        return R.ok("修改已读成功").put("data", updateCount);
    }

    @Override
    public R haveReadAndUpdateNotify(String toUserId) {
        final R r = haveRead(toUserId);
        if (r.getCode() == 0) {
            notifyService.notify(UserSecurityUtil.getCurrUserId());
        }
        return r;
    }

    @Override
    public Integer getNoReadCount(String toUserId) {
        final QueryWrapper<PersonalMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", toUserId);
        queryWrapper.eq("to_user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("is_read", 0);
        final Integer count = personalMessageDao.selectCount(queryWrapper);
        return count;
    }
}