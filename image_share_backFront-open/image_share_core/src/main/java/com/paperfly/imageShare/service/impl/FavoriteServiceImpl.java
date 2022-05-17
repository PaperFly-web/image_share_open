package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.entity.FavlistEntity;
import com.paperfly.imageShare.service.FavlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.FavoriteDao;
import com.paperfly.imageShare.entity.FavoriteEntity;
import com.paperfly.imageShare.service.FavoriteService;
import org.springframework.transaction.annotation.Transactional;


@Service("favoriteService")
@Transactional
public class FavoriteServiceImpl extends ServiceImpl<FavoriteDao, FavoriteEntity> implements FavoriteService {

    @Autowired
    FavoriteDao favoriteDao;

    @Autowired
    FavlistService favlistService;

    @Override
    public R getCurrUserFavorite(Page<FavoriteEntity> page) {
        final QueryWrapper<FavoriteEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        final Page<FavoriteEntity> resultPage = favoriteDao.selectPage(page, queryWrapper);
        return R.ok("查询成功").put("data",resultPage);
    }

    //收藏夹可以重复，不管了
    @Override
    public R add(FavoriteEntity favorite) {
        //1.检查收藏夹名称不能超过30个字
        if(EmptyUtil.empty(favorite.getFavoriteName()) || favorite.getFavoriteName().length()>30){
            return R.userError("收藏夹不能为空，或者不能超过30个字");
        }
        //判断收藏夹名称是否重复
        final QueryWrapper<FavoriteEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("favorite_name",favorite.getFavoriteName());
        queryWrapper.eq("user_id",UserSecurityUtil.getCurrUserId());
        final FavoriteEntity favoriteNameIsExist = this.getOne(queryWrapper);
        if(!EmptyUtil.empty(favoriteNameIsExist)){
            return R.userError(favorite.getFavoriteName()+":已经存在");
        }
        //设置用户ID和时间
        favorite.setUserId(UserSecurityUtil.getCurrUserId());
        favorite.setCreateTime(new Date());
        favorite.setUpdateTime(new Date());
        final int insertCount = favoriteDao.insert(favorite);
        if(insertCount>0){
            final FavoriteEntity favoriteEntity = this.getOne(queryWrapper);
            return R.ok("添加收藏夹成功").put("data",favoriteEntity);
        }else {
            return R.userError("添加收藏夹失败");
        }
    }

    @Override
    public R cancel(FavoriteEntity favorite) {
        //检查收藏夹ID是否为空
        if(EmptyUtil.empty(favorite.getId())){
            return R.userError("收藏夹名称为空");
        }
        favorite.setUserId(UserSecurityUtil.getCurrUserId());

        final UpdateWrapper<FavoriteEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id",favorite.getUserId());
        updateWrapper.eq("id",favorite.getId());
        final int deleteCount = favoriteDao.delete(updateWrapper);

        final UpdateWrapper<FavlistEntity> deleteFavlistWrapper = new UpdateWrapper<>();
        deleteFavlistWrapper.eq("favorite_id",favorite.getId());
        deleteFavlistWrapper.eq("user_id",favorite.getUserId());
        final boolean removeFavlistCount = favlistService.remove(deleteFavlistWrapper);
        if(deleteCount>0){
            return R.ok("删除收藏夹成功");
        }else {
            return R.userError("删除收藏夹失败");
        }
    }
}