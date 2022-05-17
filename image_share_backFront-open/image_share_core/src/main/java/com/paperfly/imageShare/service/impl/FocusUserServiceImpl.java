package com.paperfly.imageShare.service.impl;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.FileService;
import com.paperfly.imageShare.service.UserService;
import com.paperfly.imageShare.vo.FocusUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.FocusUserDao;
import com.paperfly.imageShare.entity.FocusUserEntity;
import com.paperfly.imageShare.service.FocusUserService;
import org.springframework.transaction.annotation.Transactional;


@Service("focusUserService")
@Transactional
public class FocusUserServiceImpl extends ServiceImpl<FocusUserDao, FocusUserEntity> implements FocusUserService {

    @Autowired
    FocusUserDao focusUserDao;

    @Autowired
    UserService userService;

    @Autowired
    FileService fileService;

    @Override
    public List<FocusUserEntity> getUserFocusUsers(String userId) {
        final List<FocusUserEntity> currUserFocusUsers = focusUserDao.getUserFocusUsers(userId);
        return currUserFocusUsers;
    }



    @Override
    public R focus(String focusUserId) {
        //1.检查被关注的用户ID是否为数字
        if (!Validator.isNumber(focusUserId)) {
            return R.userError("被关注用户ID非法");
        }
        //检查和当前用户ID是否相同
        if (focusUserId.equals(UserSecurityUtil.getCurrUserId())) {
            return R.userError("自己不能关注自己");
        }
        //2.比较当前用户ID和被关注用户ID大小
        final String currUserId = UserSecurityUtil.getCurrUserId();
        final FocusUserEntity focusUserEntity = new FocusUserEntity();
        try {
            if (Long.valueOf(focusUserId) > Long.valueOf(currUserId)) {
                focusUserEntity.setUserIdOne(currUserId);
                focusUserEntity.setUserIdTwo(focusUserId);
                focusUserEntity.setType(1);
            } else {
                focusUserEntity.setUserIdOne(focusUserId);
                focusUserEntity.setUserIdTwo(currUserId);
                focusUserEntity.setType(2);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.userError("用户ID非法");
        }

        //检查用户是否已经关注过
        final QueryWrapper<FocusUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "user_id_one", "user_id_two", "type");
        queryWrapper.eq("user_id_one", focusUserEntity.getUserIdOne());
        queryWrapper.eq("user_id_two", focusUserEntity.getUserIdTwo());
//        queryWrapper.eq("type",focusUserEntity.getType());
        FocusUserEntity hasFocus = focusUserDao.selectOne(queryWrapper);
        if (!EmptyUtil.empty(hasFocus) &&
                (Objects.equals(focusUserEntity.getType(), hasFocus.getType()) || hasFocus.getType() == 3)) {
            return R.userError("您已经关注过");
        }
        //判断是否为互相关注
        if (!EmptyUtil.empty(hasFocus)) {
            final UpdateWrapper<FocusUserEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id_one", focusUserEntity.getUserIdOne());
            updateWrapper.eq("user_id_two", focusUserEntity.getUserIdTwo());
            updateWrapper.set("type", 3);
            if (hasFocus.getType() == 1) {
                updateWrapper.set("focus_time_two", new Date());
            } else {
                updateWrapper.set("focus_time_one", new Date());
            }
            updateWrapper.set("update_time", new Date());

            final int updateCount = focusUserDao.update(null, updateWrapper);
            if (updateCount > 0) {
                return R.ok("关注用户成功").put("data",focusUserEntity);
            }
            //否为为单方面关注
        } else {
            focusUserEntity.setUpdateTime(new Date());
            focusUserEntity.setCreateTime(new Date());
            //设置单方面关注时间
            if (focusUserEntity.getType() == 1) {
                focusUserEntity.setFocusTimeOne(new Date());
            } else {
                focusUserEntity.setFocusTimeTwo(new Date());
            }
            final int insertCount = focusUserDao.insert(focusUserEntity);
            if (insertCount > 0) {
                return R.ok("关注用户成功").put("data",focusUserEntity);
            }
        }
        return R.error("不知道为何，关注失败");
    }

    @Override
    public R cancel(String focusUserId) {
        //1.检查被取消关注的用户ID是否为数字
        if (!Validator.isNumber(focusUserId)) {
            return R.userError("取消被关注用户ID非法");
        }
        //检查和当前用户ID是否相同
        if (focusUserId.equals(UserSecurityUtil.getCurrUserId())) {
            return R.userError("自己不能取消关注自己");
        }

        // 2.比较当前用户ID和被取消关注用户ID大小
        final String currUserId = UserSecurityUtil.getCurrUserId();
        final FocusUserEntity focusUserEntity = new FocusUserEntity();
        try {
            if (Long.valueOf(focusUserId) > Long.valueOf(currUserId)) {
                focusUserEntity.setUserIdOne(currUserId);
                focusUserEntity.setUserIdTwo(focusUserId);
                focusUserEntity.setType(1);
            } else {
                focusUserEntity.setUserIdOne(focusUserId);
                focusUserEntity.setUserIdTwo(currUserId);
                focusUserEntity.setType(2);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.userError("被关注用户ID非法");
        }

        //3.先尝试删除，如果删除失败：说明可能是互相关注，在尝试修改
        UpdateWrapper<FocusUserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id_one", focusUserEntity.getUserIdOne());
        updateWrapper.eq("user_id_two", focusUserEntity.getUserIdTwo());
        updateWrapper.eq("type", focusUserEntity.getType());
        final int deleteCount = focusUserDao.delete(updateWrapper);
        if (deleteCount > 0) {
            return R.ok("取消关注成功");
        }

        //尝试修改
        updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id_one", focusUserEntity.getUserIdOne());
        updateWrapper.eq("user_id_two", focusUserEntity.getUserIdTwo());
        updateWrapper.eq("type", 3);
        updateWrapper.set("type", focusUserEntity.getType() == 1 ? 2 : 1);
        if(focusUserEntity.getType() == 1){
            updateWrapper.set("focus_time_one",null);
        }else {
            updateWrapper.set("focus_time_two",null);
        }
        updateWrapper.set("update_time", new Date());
        final int updateCount = focusUserDao.update(null, updateWrapper);
        if (updateCount > 0) {
            return R.ok("取消关注用户成功");
        }

        return R.userError("取消关注失败，可能你并为关注【" + focusUserId + "】用户");
    }

    @Override
    public R getUserFocusPageById(String userId, Page<FocusUserEntity> page) {
        //1.判断用户ID是否为空
        if(EmptyUtil.empty(userId)){
            return R.userError("用户ID为空");
        }
        //分页查询关注的用户
        final IPage<FocusUserEntity> focusUsersPage = focusUserDao.getUserFocusUsersPage(page, userId);

        final Page<FocusUserVO> finallyResPage = new Page<>();
        BeanUtils.copyProperties(focusUsersPage,finallyResPage);
        //获取用户信息
        final List<FocusUserVO> focusUserVOS = focusUsersPage.getRecords().stream().map(focusUserEntity -> {
            String userIdOne = focusUserEntity.getUserIdOne();
            String userIdTwo = focusUserEntity.getUserIdTwo();
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "state", "snake_name", "username", "signature", "sex", "head_image");
            FocusUserVO focusUserVO = new FocusUserVO();
            focusUserVO.setId(focusUserEntity.getId());
            UserEntity user;
            //判断和当前用户的关系
            if(userId.equals(userIdOne)){
                focusUserVO.setFocusType(getUserOneFocusUserTwoFocusType(UserSecurityUtil.getCurrUserId(),userIdTwo));
            }else {
                focusUserVO.setFocusType(getUserOneFocusUserTwoFocusType(UserSecurityUtil.getCurrUserId(),userIdOne));
            }

            if (userId.equals(userIdOne)) {
                focusUserVO.setFocusTime(focusUserEntity.getFocusTimeOne());
                queryWrapper.eq("id", userIdTwo);
                user = userService.getOne(queryWrapper);
                user.setHeadImage(fileService.getFileUrl(user.getHeadImage(), false));
            } else {
                focusUserVO.setFocusTime(focusUserEntity.getFocusTimeTwo());
                queryWrapper.eq("id", userIdOne);
                user = userService.getOne(queryWrapper);
                user.setHeadImage(fileService.getFileUrl(user.getHeadImage(), false));
            }
            focusUserVO.setUserTwo(user);
            return focusUserVO;
        }).collect(Collectors.toList());
        finallyResPage.setRecords(focusUserVOS);
        return R.ok("查询成功").put("data",finallyResPage);
    }

    @Override
    public R getUserFansPageById(String userId, Page<FocusUserEntity> page) {
        //1.判断用户ID是否为空
        if(EmptyUtil.empty(userId)){
            return R.userError("用户ID为空");
        }
        //分页查询关注的用户
        final IPage<FocusUserEntity> focusUsersPage = focusUserDao.getUserFansUsersPage(page, userId);

        final Page<FocusUserVO> finallyResPage = new Page<>();
        BeanUtils.copyProperties(focusUsersPage,finallyResPage);
        //获取用户信息
        final List<FocusUserVO> focusUserVOS = focusUsersPage.getRecords().stream().map(focusUserEntity -> {
            String userIdOne = focusUserEntity.getUserIdOne();
            String userIdTwo = focusUserEntity.getUserIdTwo();
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "state", "snake_name", "username", "signature", "sex", "head_image");
            FocusUserVO focusUserVO = new FocusUserVO();
            focusUserVO.setId(focusUserEntity.getId());
            UserEntity user;
            focusUserVO.setFocusTime(focusUserEntity.getCreateTime());
            //判断和当前用户的关系
            if(userId.equals(userIdOne)){
                focusUserVO.setFocusType(getUserOneFocusUserTwoFocusType(UserSecurityUtil.getCurrUserId(),userIdTwo));
            }else {
                focusUserVO.setFocusType(getUserOneFocusUserTwoFocusType(UserSecurityUtil.getCurrUserId(),userIdOne));
            }
            if (userId.equals(userIdOne)) {
                queryWrapper.eq("id", userIdTwo);
                user = userService.getOne(queryWrapper);
                user.setHeadImage(fileService.getFileUrl(user.getHeadImage(), false));
            } else {
                queryWrapper.eq("id", userIdOne);
                user = userService.getOne(queryWrapper);
                user.setHeadImage(fileService.getFileUrl(user.getHeadImage(), false));

            }
            focusUserVO.setUserTwo(user);
            return focusUserVO;
        }).collect(Collectors.toList());
        finallyResPage.setRecords(focusUserVOS);
        return R.ok("查询成功").put("data",finallyResPage);
    }

    @Override
    public Long getFansCount(String userId) {
        return focusUserDao.getFansCount(userId);
    }

    @Override
    public Long getFocusCount(String userId) {
        return focusUserDao.getFocusCount(userId);
    }

    @Override
    public R userIsFocus(String userIdOne, String userIdTwo) {
        final R res = this.getUserIdOneFocusUserIdTwo(userIdOne, userIdTwo);
        if((int)res.getCode() != 0){
            return R.ok(res.getMsg()).put("data",false).put("focusInfo",res.getData());
        }else {
            return R.ok(res.getMsg()).put("data",true).put("focusInfo",res.getData());
        }
    }

    @Override
    public R getUserIdOneFocusUserIdTwo(String userIdOne, String userIdTwo) {
        //1.检查用户ID是否为数字
        if (!Validator.isNumber(userIdOne) || !Validator.isNumber(userIdTwo)) {
            return R.userError("用户ID非法");
        }
        //检查用户userIdOne和用户userIdTwo是否相同
        if (userIdOne.equals(userIdTwo)) {
            return R.userError("用户ID相同");
        }

        // 2.比较用户userIdOne和用户userIdTwo大小
        FocusUserEntity focusUserEntity = new FocusUserEntity();
        try {
            if (Long.valueOf(userIdOne) < Long.valueOf(userIdTwo)) {
                focusUserEntity.setUserIdOne(userIdOne);
                focusUserEntity.setUserIdTwo(userIdTwo);
                focusUserEntity.setType(1);
            } else {
                focusUserEntity.setUserIdOne(userIdTwo);
                focusUserEntity.setUserIdTwo(userIdOne);
                focusUserEntity.setType(2);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.userError("被关注用户ID非法");
        }

        FocusUserEntity result = focusUserDao.getUserFocusUser(focusUserEntity);
        if(EmptyUtil.empty(result)){
            return R.userError("【"+userIdOne+"】还未关注【"+userIdTwo+"】");
        }
        return R.ok("查询成功").put("data",result);
    }

    /**
     * 获取userOneId对userTwoId的关注类型
     * @param userOneId
     * @param userTwoId
     * @return
     */
    public Integer getUserOneFocusUserTwoFocusType(String userOneId,String userTwoId){

        final R res = this.getUserIdOneFocusUserIdTwo(userOneId, userTwoId);
        //判断是否有关注信息
        if((int)res.getCode()!=0){
            //什么都没关注
            return 0;
        }

        FocusUserEntity focusUser = (FocusUserEntity) res.getData();
        //互相关注
        if(focusUser.getType() == 3){
            return 3;
        }
        //判断是否为单项关注
        //上面获取关注信息时候，已经判断过是否可以转换为数字,是否相等
        if(Long.valueOf(userOneId)<Long.valueOf(userTwoId)){
            if(focusUser.getType() == 1){
                //userOneId关注userTwoId
                return 1;
            }else {
                //userTwoId关注userOneId
                return 2;
            }
        }else {
            if(focusUser.getType() == 1){
                //userTwoId关注userOneId
                return 2;
            }else {
                //userOneId关注userTwoId
                return 1;
            }
        }
    }
}