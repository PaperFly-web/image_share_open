package com.paperfly.imageShare.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.constant.NotifyConst;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.entity.*;
import com.paperfly.imageShare.service.*;
import com.paperfly.imageShare.vo.NotifyTipsVO;
import com.paperfly.imageShare.websocket.sendone.WsNotifyService;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.NotifyDao;
import org.springframework.transaction.annotation.Transactional;


@Service("notifyService")
@Transactional
@Aspect
public class NotifyServiceImpl extends ServiceImpl<NotifyDao, NotifyEntity> implements NotifyService {

    @Autowired
    NotifyDao notifyDao;

    @Autowired
    NotifyConfigService notifyConfigService;

    @Autowired
    WsNotifyService wsNotifyService;

    @Autowired
    PostService postService;

    @Autowired
    PostCommentService postCommentService;

    @Autowired
    UserService userService;

    @Autowired
    FocusUserService focusUserService;

    @Autowired
    BlackUserService blackUserService;

    @Override
    public R addAndNotify(NotifyEntity notify) {
        if (EmptyUtil.empty(notify)) {
            return R.userError("保存消息通知失败，消息通知体为空");
        }
        //添加时间
        notify.setCreateTime(new Date());
        notify.setUpdateTime(new Date());
        notify.setIsRead(0);
        int insertCount = 0;
        if (isSave(notify)) {
            insertCount = notifyDao.insert(notify);
        }
        if (insertCount > 0) {
            //如果用户在线，就通知用户
            this.notify(notify.getUserId());
            return R.ok("保存消息通知成功");
        } else {
            return R.error("不知道为保存消息通知失败,可能用户配置不需要保存，或者用户不存在");
        }
    }

    @Override
    public R addFocusAndNotify(FocusUserEntity focusUser) {
        //检查被关注与关注用户ID是否为空
        if (EmptyUtil.empty(focusUser)) {
            return R.userError("实体类为空");
        }
        /*if (EmptyUtil.empty(focusUser.getUserIdTwo())) {
            return R.userError("userIdTwo为空");
        }
        if (EmptyUtil.empty(focusUser.getUserIdOne())) {
            return R.userError("userIdOne为空");
        }
        if (EmptyUtil.empty(focusUser.getType()) ||
                !focusUser.getType().equals(1) ||
                !focusUser.getType().equals(2)) {
            return R.userError("关注类型错误");
        }*/
        final NotifyEntity notifyEntity = new NotifyEntity();
        //设置消息发送者与接受者
        if (focusUser.getType() == 1) {
            notifyEntity.setSenderId(focusUser.getUserIdOne());
            notifyEntity.setUserId(focusUser.getUserIdTwo());
        }else {
            notifyEntity.setSenderId(focusUser.getUserIdTwo());
            notifyEntity.setUserId(focusUser.getUserIdOne());
        }
        //设置消息内容
//        notifyEntity.setContent("");
        notifyEntity.setType(NotifyConst.Type.REMIND.getType());
        notifyEntity.setTargetId(notifyEntity.getUserId());
        notifyEntity.setTargetType(NotifyConst.TargetType.ONE_USER.getTargetType());
        notifyEntity.setAction(NotifyConst.Action.FOCUS.getAction());
        notifyEntity.setSenderType(NotifyConst.SenderType.USER.getSenderType());
        notifyEntity.setIsRead(0);
        notifyEntity.setCreateTime(new Date());
        notifyEntity.setUpdateTime(new Date());
        return addAndNotify(notifyEntity);
    }

    @Override
    public R addPostCommentAndNotify(PostCommentEntity postComment) {
        if(EmptyUtil.empty(postComment)){
            return R.userError("实体类为空");
        }
        final NotifyEntity notifyEntity = new NotifyEntity();
        //判断是父评论还是子评论,设置接收者用户ID
        if(EmptyUtil.empty(postComment.getFatherCommentId())){
            final QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",postComment.getPostId());
            queryWrapper.select("user_id","handle_content");
            final PostEntity postEntity = postService.getOne(queryWrapper);
            notifyEntity.setUserId(postEntity.getUserId());
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("originalContent",postEntity.getHandleContent());
            jsonObject.put("content",postComment.getHandleContent());
            notifyEntity.setContent(jsonObject.toString());
            notifyEntity.setTargetType(NotifyConst.TargetType.POST.getTargetType());
            notifyEntity.setTargetId(postComment.getPostId());
        }else {
            final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("snake_name",postComment.getReplayUserSnakeName());
            queryWrapper.select("id");
            final UserEntity userEntity = userService.getOne(queryWrapper);
            notifyEntity.setUserId(userEntity.getId());
            final JSONObject jsonObject = new JSONObject();
            //查询被回复的评论
            final QueryWrapper<PostCommentEntity> postCommentEntityQueryWrapper = new QueryWrapper<>();
            postCommentEntityQueryWrapper.eq("id",postComment.getFatherCommentId());
            postCommentEntityQueryWrapper.select("handle_content");
            final PostCommentEntity originalPostComment = postCommentService.getOne(postCommentEntityQueryWrapper);
            jsonObject.put("originalContent",originalPostComment.getHandleContent());
            jsonObject.put("content",postComment.getHandleContent());
            notifyEntity.setTargetType(NotifyConst.TargetType.COMMENT.getTargetType());
            notifyEntity.setContent(jsonObject.toString());
            notifyEntity.setTargetId(postComment.getFatherCommentId());
        }

        notifyEntity.setType(NotifyConst.Type.REMIND.getType());
        notifyEntity.setAction(NotifyConst.Action.COMMENT.getAction());
        notifyEntity.setSenderId(postComment.getUserId());
        notifyEntity.setSenderType(NotifyConst.SenderType.USER.getSenderType());
        notifyEntity.setIsRead(0);
        notifyEntity.setCreateTime(new Date());
        notifyEntity.setUpdateTime(new Date());
        //判断是不是对自己的
        if(UserSecurityUtil.getCurrUserId().equals(notifyEntity.getUserId())){
            return R.ok("自己对自己的评论不需要通知");
        }
        return addAndNotify(notifyEntity);
    }

    @Override
    public R addThumbAndNotify(ThumbEntity thumb) {
        if(EmptyUtil.empty(thumb)){
            return R.userError("实体类为空");
        }
        final NotifyEntity notifyEntity = new NotifyEntity();
        if(thumb.getThumbType() == 0){
            final QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",thumb.getToId());
            queryWrapper.select("user_id","handle_content");
            final PostEntity postEntity = postService.getOne(queryWrapper);
            //获取被点赞的帖子内容，以及被点赞的帖子用户ID
            notifyEntity.setContent(postEntity.getHandleContent());
            notifyEntity.setUserId(postEntity.getUserId());
            notifyEntity.setTargetType(NotifyConst.TargetType.POST.getTargetType());
        }else {
            //查询被回复的评论内容,和被回复用户的ID
            final QueryWrapper<PostCommentEntity> postCommentEntityQueryWrapper = new QueryWrapper<>();
            postCommentEntityQueryWrapper.eq("id",thumb.getToId());
            postCommentEntityQueryWrapper.select("handle_content","user_id");
            final PostCommentEntity postCommentEntity = postCommentService.getOne(postCommentEntityQueryWrapper);
            //被回复的用户评论
            notifyEntity.setContent(postCommentEntity.getHandleContent());
            //被回复的用户ID
            notifyEntity.setUserId(postCommentEntity.getUserId());
            notifyEntity.setTargetType(NotifyConst.TargetType.COMMENT.getTargetType());
        }
        notifyEntity.setType(NotifyConst.Type.REMIND.getType());
        notifyEntity.setTargetId(thumb.getToId());
        notifyEntity.setAction(NotifyConst.Action.THUMB.getAction());
        notifyEntity.setSenderId(thumb.getUserId());
        notifyEntity.setSenderType(NotifyConst.SenderType.USER.getSenderType());
        notifyEntity.setIsRead(0);
        notifyEntity.setCreateTime(new Date());
        notifyEntity.setUpdateTime(new Date());

        //判断是不是对自己的
        if(UserSecurityUtil.getCurrUserId().equals(notifyEntity.getUserId())){
            return R.ok("自己对自己的点赞不需要通知");
        }
        return addAndNotify(notifyEntity);
    }

    @Override
    public R addPersonalMessageAndNotify(PersonalMessageEntity personalMessageEntity) {
        final NotifyEntity notifyEntity = new NotifyEntity();
        notifyEntity.setContent(personalMessageEntity.getContent());
        notifyEntity.setType(NotifyConst.Type.MESSAGE.getType());
        notifyEntity.setTargetId(personalMessageEntity.getToUserId());
        notifyEntity.setTargetType(NotifyConst.TargetType.ONE_USER.getTargetType());
        notifyEntity.setAction(NotifyConst.Action.PERSONAL_MESSAGE.getAction());
        notifyEntity.setSenderId(personalMessageEntity.getUserId());
        notifyEntity.setSenderType(NotifyConst.SenderType.USER.getSenderType());
        notifyEntity.setIsRead(0);
        notifyEntity.setUserId(personalMessageEntity.getToUserId());
        notifyEntity.setCreateTime(new Date());
        notifyEntity.setUpdateTime(new Date());
        return addAndNotify(notifyEntity);
    }


    @Override
    public R notify(NotifyTipsVO notifyTipsVO) {
        wsNotifyService.sendTo(notifyTipsVO);
        return R.ok("发送消息通知成功");
    }

    //给在线用户通知简短消息
    @Override
    public R notify(String userId) {
        final QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_read", 0);
        final int notifyCount = this.count(queryWrapper);
        final NotifyTipsVO notifyTipsVO = new NotifyTipsVO();
        notifyTipsVO.setDesc("有" + notifyCount + "条新消息");
        notifyTipsVO.setCount(notifyCount);
        notifyTipsVO.setUserId(userId);
        return this.notify(notifyTipsVO);
    }

    @Override
    public R personalMessageHaveRead(String userId) {
        final UpdateWrapper<NotifyEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        updateWrapper.eq("sender_id", userId);
        updateWrapper.eq("type", NotifyConst.Type.MESSAGE.getType());
        updateWrapper.eq("action",NotifyConst.Action.PERSONAL_MESSAGE.getAction());
        updateWrapper.eq("target_type",NotifyConst.TargetType.ONE_USER.getTargetType());
        updateWrapper.set("is_read", 1);
        final int updateCount = notifyDao.update(null, updateWrapper);
        return R.ok("修改已读成").put("data", updateCount);
    }

    @Override
    public R systemMessageHaveRead(String userId) {
        if(EmptyUtil.empty(userId)){
            userId = UserSecurityUtil.getCurrUserId();
        }
        final UpdateWrapper<NotifyEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        updateWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        updateWrapper.eq("action",NotifyConst.Action.SYSTEM_MESSAGE.getAction());
        updateWrapper.set("is_read", 1);
        final int updateCount = notifyDao.update(null, updateWrapper);
        this.notify(userId);
        return R.ok("修改已读成").put("data", updateCount);
    }

    @Override
    public R thumbHaveReadAndNotify(String userId) {
        if(EmptyUtil.empty(userId)){
            userId = UserSecurityUtil.getCurrUserId();
        }
        final UpdateWrapper<NotifyEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId);
        updateWrapper.eq("action", NotifyConst.Action.THUMB.getAction());
        updateWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        updateWrapper.set("is_read", 1);
//        final boolean updateCount = this.update(updateWrapper);
        final int updateCount = notifyDao.update(null, updateWrapper);
        this.notify(userId);
        return R.ok("修改已读成").put("data", updateCount);
    }

    @Override
    public R postCommentHaveReadAndNotify(String userId) {
        if(EmptyUtil.empty(userId)){
            userId = UserSecurityUtil.getCurrUserId();
        }
        final UpdateWrapper<NotifyEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId);
        updateWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        updateWrapper.eq("action", NotifyConst.Action.COMMENT.getAction());
        updateWrapper.set("is_read", 1);
//        final boolean updateCount = this.update(updateWrapper);
        final int updateCount = notifyDao.update(null, updateWrapper);
        this.notify(userId);
        return R.ok("修改已读成").put("data", updateCount);
    }

    @Override
    public R focusHaveReadAndNotify(String userId) {
        if(EmptyUtil.empty(userId)){
            userId = UserSecurityUtil.getCurrUserId();
        }
        final UpdateWrapper<NotifyEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId);
        updateWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        updateWrapper.eq("action", NotifyConst.Action.FOCUS.getAction());
        updateWrapper.set("is_read", 1);
        final int updateCount = notifyDao.update(null, updateWrapper);
        this.notify(userId);
        return R.ok("修改已读成").put("data", updateCount);
    }

    @Override
    public R categoryCount() {
        //1.查询私信数量
        QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("type", NotifyConst.Type.MESSAGE.getType());
        queryWrapper.eq("action",NotifyConst.Action.PERSONAL_MESSAGE);
        queryWrapper.eq("target_type",NotifyConst.TargetType.ONE_USER);
        queryWrapper.eq("is_read", 0);
        final Integer personalMessageCount = notifyDao.selectCount(queryWrapper);
        //2.查询评论数量
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        queryWrapper.eq("action", NotifyConst.Action.COMMENT.getAction());
        queryWrapper.eq("is_read", 0);
        final Integer postCommentCount = notifyDao.selectCount(queryWrapper);
        //3.查询点赞数量
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("action", NotifyConst.Action.THUMB.getAction());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        queryWrapper.eq("is_read", 0);
        final Integer thumbCount = notifyDao.selectCount(queryWrapper);
        //4.查询关注数量
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        queryWrapper.eq("action", NotifyConst.Action.FOCUS.getAction());
        queryWrapper.eq("is_read", 0);
        final Integer focusCount = notifyDao.selectCount(queryWrapper);
        //4.查询系统消息数量
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        queryWrapper.eq("action", NotifyConst.Action.SYSTEM_MESSAGE.getAction());
        queryWrapper.eq("is_read", 0);
        final Integer systemCount = notifyDao.selectCount(queryWrapper);

        final NotifyTipsVO notifyTipsVO = new NotifyTipsVO();
        notifyTipsVO.setFocusCount(focusCount);
        notifyTipsVO.setThumbCount(thumbCount);
        notifyTipsVO.setPostCommentCount(postCommentCount);
        notifyTipsVO.setPersonalMessageCount(personalMessageCount);
        notifyTipsVO.setSystemMessageCount(systemCount);
        notifyTipsVO.setUserId(UserSecurityUtil.getCurrUserId());
        return R.ok("查询消息通知数量成功").put("data",notifyTipsVO);
    }

    @Override
    public R focusDetails(Page<NotifyEntity> page) {
        QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        queryWrapper.eq("action", NotifyConst.Action.FOCUS.getAction());
        final Page<NotifyEntity> resPage = this.page(page, queryWrapper);
        return R.ok("查询关注通知详情成功").put("data",resPage);
    }

    @Override
    public R postCommentDetails(Page<NotifyEntity> page) {
        QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        queryWrapper.eq("action", NotifyConst.Action.COMMENT.getAction());

        final Page<NotifyEntity> resPage = this.page(page, queryWrapper);
        return R.ok("查询评论通知详情成功").put("data",resPage);
    }

    @Override
    public R thumbNotifyDetails(Page<NotifyEntity> page) {
        QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("action", NotifyConst.Action.THUMB.getAction());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        final Page<NotifyEntity> resPage = this.page(page, queryWrapper);
        return R.ok("查询点赞通知详情成功").put("data",resPage);
    }

    @Override
    public R systemMessageNotifyDetails(Page<NotifyEntity> page) {
        QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("action", NotifyConst.Action.SYSTEM_MESSAGE.getAction());
        queryWrapper.eq("type", NotifyConst.Type.REMIND.getType());
        final Page<NotifyEntity> resPage = this.page(page, queryWrapper);
        return R.ok("查询系统消息通知详情成功").put("data",resPage);
    }

    @Override
    public R deleteNotifyById(String id) {
        if(EmptyUtil.empty(id)){
            return R.userError("ID不能为空");
        }
        final UpdateWrapper<NotifyEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id",UserSecurityUtil.getCurrUserId());
        updateWrapper.eq("id",id);
        final int deleteCount = notifyDao.delete(updateWrapper);
        if(deleteCount>0){
            return R.ok("删除成功");
        }else {
            return R.error("删除失败，可能ID错误");
        }
    }


    //暂时默认都保存，以后有时间在升级
    //判断是否应该保存到消息通知库
    private boolean isSave(NotifyEntity notify) {
        if (EmptyUtil.empty(notify)) {
            return false;
        }
        if (EmptyUtil.empty(notify.getUserId())) {
            return false;
        }
        //如果是通知公告，就可以直接保存
        if (NotifyConst.Type.ANNOUNCE.getType().equals(notify.getType())) {
            return true;
        }
        /*//如果是系统消息，就可以直接保存
        if (NotifyConst.Action.SYSTEM_MESSAGE.getAction().equals(notify.getAction())) {
            return true;
        }*/

        //判断发送者ID是否为当前用户的黑名单
        if(blackUserService.isBlackUserMember(notify.getSenderId())){
            return false;
        }
        //获取用户的消息配置
        final String userId = notify.getUserId();
        final NotifyConfigEntity userNotifyConfig = notifyConfigService.getUserNotifyConfig(userId);
        //消息配置等级
        Integer notifyLevel = 0;
        //判断是不是点赞的
        if(NotifyConst.Action.THUMB.getAction().equals(notify.getAction())){
            notifyLevel = userNotifyConfig.getThumb();
        }
        //判断评论
        if(NotifyConst.Action.COMMENT.getAction().equals(notify.getAction())){
            notifyLevel = userNotifyConfig.getComment();
        }
        //关注
        if(NotifyConst.Action.FOCUS.getAction().equals(notify.getAction())){
            notifyLevel = userNotifyConfig.getFollow();
        }
        //私信
        if(NotifyConst.Action.PERSONAL_MESSAGE.getAction().equals(notify.getAction())){
            notifyLevel = userNotifyConfig.getPersonalMessage();
        }
        //如果是系统消息
        if(NotifyConst.Action.SYSTEM_MESSAGE.getAction().equals(notify.getAction())){
            notifyLevel = userNotifyConfig.getSystemMessage();
        }
        //如果是关闭
        if(notifyLevel == 0){
            return false;
            //如果是全员放开
        }else if(notifyLevel == 2){
            return true;
        }else {//如果是关注列表
            //获取用户的关注列表用户ID
            final List<String> focusUserIds = focusUserService.getUserFocusUsers(userId).stream().map(x -> {
                if (userId.equals(x.getUserIdTwo())) {
                    return x.getUserIdOne();
                } else {
                    return x.getUserIdTwo();
                }
            }).collect(Collectors.toList());
            return focusUserIds.contains(notify.getSenderId());
        }
    }


}