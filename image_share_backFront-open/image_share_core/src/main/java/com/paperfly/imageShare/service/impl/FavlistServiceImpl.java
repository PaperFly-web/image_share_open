package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.entity.FavoriteEntity;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.PostService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.FavlistDao;
import com.paperfly.imageShare.entity.FavlistEntity;
import com.paperfly.imageShare.service.FavlistService;
import org.springframework.transaction.annotation.Transactional;


@Service("favlistService")
@Transactional
public class FavlistServiceImpl extends ServiceImpl<FavlistDao, FavlistEntity> implements FavlistService {

    @Autowired
    PostService postService;

    @Autowired
    FavlistDao favlistDao;

    @Override
    public R add(FavlistEntity favlist) {
        //1.检查收藏的帖子是否为空
        if (EmptyUtil.empty(favlist.getPostId())) {
            return R.userError("收藏的帖子ID不能为空");
        }
        //设置用户ID
        favlist.setUserId(UserSecurityUtil.getCurrUserId());
        //设置时间
        favlist.setCreateTime(new Date());
        favlist.setUpdateTime(new Date());
        //判断用户是否收藏了此帖子
        final QueryWrapper<FavlistEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", favlist.getUserId());
        queryWrapper.eq("post_id", favlist.getUserId());
        final FavlistEntity currUserIsFavlist = this.getOne(queryWrapper);
        if (!EmptyUtil.empty(currUserIsFavlist)) {
            return R.userError("您已经收藏过此帖子");
        }
        final int insertCount = favlistDao.insert(favlist);
        if (insertCount > 0) {
            final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", favlist.getPostId());
            updateWrapper.set("update_time",new Date());
            updateWrapper.setSql("favorite_count=favorite_count+1");
            postService.update(updateWrapper);
            return R.ok("收藏成功");
        } else {
            return R.userError("收藏失败，可能重复收藏");
        }
    }

    @Override
    public R cancel(FavlistEntity favlist) {
        //1.检查的帖子是否为空
        if (EmptyUtil.empty(favlist.getPostId())) {
            return R.userError("要取消收藏的帖子ID不能为空");
        }
        //设置用户ID
        favlist.setUserId(UserSecurityUtil.getCurrUserId());

        final UpdateWrapper<FavlistEntity> deleteWrapper = new UpdateWrapper<>();
        deleteWrapper.eq("post_id", favlist.getPostId());
        deleteWrapper.eq("user_id", favlist.getUserId());
        final int deleteCount = favlistDao.delete(deleteWrapper);
        if (deleteCount > 0) {
            final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", favlist.getPostId());
            updateWrapper.setSql("favorite_count =favorite_count-1");
            postService.update(updateWrapper);
            return R.ok("取消收藏成功");
        } else {
            return R.error("收藏失败,可能要取消的收藏不存在");
        }
    }

    @Override
    public R currUserIsFavPost(String postId) {
        //1.判断帖子ID是否为空
        if (EmptyUtil.empty(postId)) {
            return R.ok("没有收藏，因为帖子ID为空").put("data", false);
        }
        final QueryWrapper<FavlistEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId);
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        final FavlistEntity favlistEntity = favlistDao.selectOne(queryWrapper);
        if (EmptyUtil.empty(favlistEntity)) {
            return R.ok("没有收藏").put("data", false);
        } else {
            return R.ok("已经收藏").put("data", true).put("orginalData", favlistEntity);
        }
    }

    @Override
    public R getCurrUserFavlistPage(Page<FavlistEntity> page, String favId) {
        final QueryWrapper<FavlistEntity> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        if (EmptyUtil.empty(favId) || "null".equals(favId)) {
            queryWrapper.isNull("favorite_id");
        } else {
            queryWrapper.eq("favorite_id", favId);
        }
        final Page<FavlistEntity> resultPage = favlistDao.selectPage(page, queryWrapper);
        final Page<PostEntity> finallyResultPage = new Page<>();
        if (resultPage.getTotal() > 0) {
            final List<String> postIds = resultPage.getRecords().stream().map(x -> {
                return x.getPostId();
            }).collect(Collectors.toList());
            final R r = postService.getPost(postIds);
            if ((int) r.getCode() != 0) {
                return R.ok("查询成功，但没找到数据").put("data", finallyResultPage);
            } else {
                BeanUtils.copyProperties(resultPage, finallyResultPage);
                finallyResultPage.setRecords((List<PostEntity>) r.getData());
                return R.ok("查询成功").put("data", postService.postEntityPageToPostDTOPage(finallyResultPage));
            }
        } else {
            return R.ok("查询成功,但没有数据").put("data", finallyResultPage);
        }
    }


}