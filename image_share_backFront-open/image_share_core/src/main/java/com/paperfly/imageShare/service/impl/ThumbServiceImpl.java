package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.dao.PostDao;
import com.paperfly.imageShare.dto.ThumbDTO;
import com.paperfly.imageShare.entity.PostCommentEntity;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.PostCommentService;
import com.paperfly.imageShare.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.ThumbDao;
import com.paperfly.imageShare.entity.ThumbEntity;
import com.paperfly.imageShare.service.ThumbService;
import org.springframework.transaction.annotation.Transactional;


@Service("thumbService")
@Transactional
public class ThumbServiceImpl extends ServiceImpl<ThumbDao, ThumbEntity> implements ThumbService {

    @Autowired
    ThumbDao thumbDao;

    @Autowired
    PostService postService;

    @Autowired
    PostCommentService postCommentService;


    @Override
    public R cancel(ThumbEntity thumb) {
        //1.检测点赞类型对不对
        if (EmptyUtil.empty(thumb.getThumbType()) || (thumb.getThumbType() != 1 && thumb.getThumbType() != 0)) {
            return R.userError("点赞类型错误,只有帖子与评论可以点赞");
        }
        //2.检查点赞的帖子或评论ID是否为空
        if (EmptyUtil.empty(thumb.getToId())) {
            return R.userError("被点赞帖子或评论ID为空");
        }
        final UpdateWrapper<ThumbEntity> deleteWrapper = new UpdateWrapper<>();
        deleteWrapper.eq("to_id", thumb.getToId());
        deleteWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        deleteWrapper.eq("thumb_type", thumb.getThumbType());
        final int deleteCount = thumbDao.delete(deleteWrapper);
        //判断是否删除点赞数据，如果没删除则不同步帖子的点赞数量。
        //toId,userId,thumbType三个组成唯一键，保证只能删除一个数据
        if (thumb.getThumbType() == 0 && deleteCount > 0) {
            final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", thumb.getToId());
            updateWrapper.setSql("thumb_count=thumb_count-1");
            postService.update(updateWrapper);
        } else if (thumb.getThumbType() == 1 && deleteCount > 0) {
            final UpdateWrapper<PostCommentEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", thumb.getToId());
            updateWrapper.setSql("thumb_count=thumb_count-1");
            postCommentService.update(updateWrapper);
        }
        if(deleteCount<=0){
            return R.userError("取消点赞失败");
        }
        return R.ok("取消点赞成功");
    }

    @Override
    public R add(ThumbEntity thumb) {
        //1.检测点赞类型对不对
        if (EmptyUtil.empty(thumb.getThumbType()) || (thumb.getThumbType() != 1 && thumb.getThumbType() != 0)) {
            return R.userError("点赞类型错误,只有帖子与评论可以点赞");
        }
        //2.检查点赞的帖子或评论ID是否为空
        if (EmptyUtil.empty(thumb.getToId())) {
            return R.userError("被点赞帖子或评论ID为空");
        }
        thumb.setId(null);
        thumb.setUserId(UserSecurityUtil.getCurrUserId());
        thumb.setCreateTime(new Date());
        thumb.setUpdateTime(new Date());
        final QueryWrapper<ThumbEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_id", thumb.getToId());
        queryWrapper.eq("user_id", thumb.getUserId());
        queryWrapper.eq("thumb_type", thumb.getThumbType());
        final ThumbEntity thumbEntity = thumbDao.selectOne(queryWrapper);
        //因为只涉及到一个用户，没有什么并发
        if (!EmptyUtil.empty(thumbEntity)) {
            return R.userError("您已经点赞过");
        }
        //toId,userId,thumbType三个组成唯一键，保证一个用户只能对帖子点赞一次
        final Integer addCount = thumbDao.add(thumb);
        //检查点赞是否成功，点赞成功才把帖子/评论的点赞数量同步
        if (thumb.getThumbType() == 0 && addCount > 0) {
            final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", thumb.getToId());
            updateWrapper.setSql("thumb_count=thumb_count+1");
            postService.update(updateWrapper);
        } else if (thumb.getThumbType() == 1 && addCount > 0) {
            final UpdateWrapper<PostCommentEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", thumb.getToId());
            updateWrapper.setSql("thumb_count=thumb_count+1");
            postCommentService.update(updateWrapper);
        }
        return R.ok("点赞成功");
    }

    @Override
    public R getCountByType(String toId, Integer type) {
        //查询基本不需要数据验证，用户输入错了就错了
        final QueryWrapper<ThumbEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_id", toId);
        queryWrapper.eq("type", type);
        final Integer thumbCount = thumbDao.selectCount(queryWrapper);
        final ThumbDTO thumbDTO = new ThumbDTO();
        thumbDTO.setThumbType(type);
        thumbDTO.setToId(toId);
        thumbDTO.setCount(thumbCount);
        return R.ok("查询点赞数量成功").put("data", thumbDTO);
    }

    @Override
    public R currUserIsThumb(String toId, Integer type) {
        final QueryWrapper<ThumbEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_id", toId);
        queryWrapper.eq("thumb_type", type);
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        final ThumbEntity thumbEntity = thumbDao.selectOne(queryWrapper);
        return R.ok("查询成功").put("data", thumbEntity);
    }
}