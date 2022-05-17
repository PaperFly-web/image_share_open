package com.paperfly.imageShare.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.ViewDetailsDao;
import com.paperfly.imageShare.entity.ViewDetailsEntity;
import com.paperfly.imageShare.service.ViewDetailsService;
import org.springframework.transaction.annotation.Transactional;


@Service("viewDetailsService")
@Transactional
public class ViewDetailsServiceImpl extends ServiceImpl<ViewDetailsDao, ViewDetailsEntity> implements ViewDetailsService {

    @Autowired
    ViewDetailsDao viewDetailsDao;

    @Override
    public R add(ViewDetailsEntity viewDetails) {
        if(EmptyUtil.empty(viewDetails)){
            return R.userError("实体类为空");
        }
        viewDetails.setUserId(UserSecurityUtil.getCurrUserId());
        //检查浏览的帖子ID是否为空
        if(EmptyUtil.empty(viewDetails.getPostId())){
            return R.userError("帖子ID为空");
        }
        //检查帖子拥有者id
        if(EmptyUtil.empty(viewDetails.getPostUserId())){
            return R.userError("帖子拥有者ID为空");
        }
        if(EmptyUtil.empty(viewDetails.getViewFrom())){
            viewDetails.setViewFrom(5);
        }
        final int insertCount = viewDetailsDao.insert(viewDetails);
        if(insertCount>0){
            return R.ok("保存浏览详情成功");
        }else {
            return R.error("保存浏览详情失败");
        }
    }

    @Override
    public R getPageViews(Page<ViewDetailsEntity> page) {
        final QueryWrapper<ViewDetailsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",UserSecurityUtil.getCurrUserId());
        final Page<ViewDetailsEntity> resPage = viewDetailsDao.selectPage(page, queryWrapper);
        return R.ok("查询浏览详情成功").put("data",resPage);
    }

    @Override
    public R deleteViewsById(String id) {
        if(EmptyUtil.empty(id)){
            return R.userError("id不能为空");
        }
        final UpdateWrapper<ViewDetailsEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.eq("user_id",UserSecurityUtil.getCurrUserId());
        final int deleteCount = viewDetailsDao.delete(updateWrapper);
        if(deleteCount>0){
            return R.ok("删除浏览详情成功");
        }else {
            return R.error("删除浏览详情失败");
        }
    }
}