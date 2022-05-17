package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.BlackUserDao;
import com.paperfly.imageShare.entity.BlackUserEntity;
import com.paperfly.imageShare.service.BlackUserService;
import org.springframework.transaction.annotation.Transactional;


@Service("blackUserService")
@Transactional
public class BlackUserServiceImpl extends ServiceImpl<BlackUserDao, BlackUserEntity> implements BlackUserService {

    @Autowired
    BlackUserDao blackUserDao;

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Override
    public R getCurrUserBlackUser(Page<BlackUserEntity> page) {
        QueryWrapper<BlackUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        Page<BlackUserEntity> blackUserEntityPage = blackUserDao.selectPage(page, queryWrapper);
        return R.ok("查询成功").put("data", blackUserEntityPageToUserEntityPage(blackUserEntityPage));
    }

    @Override
    public R deleteCurrUserBlackUser(String blackUserId) {
        final UpdateWrapper<BlackUserEntity> deleteWrapper = new UpdateWrapper<>();
        deleteWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        deleteWrapper.eq("black_user_id", blackUserId);
        final int deleteCount = blackUserDao.delete(deleteWrapper);
        if (deleteCount > 0) {
            //移除redis缓存的黑名单
            redisTemplate.opsForSet().remove("BLACK_USER_" + UserSecurityUtil.getCurrUserId(), blackUserId);
            return R.ok("删除成功");
        } else {
            return R.userError("删除失败，可能ID不存在");
        }
    }

    @Override
    public R addCurrBlackUser(String blackUserId) {
        if (EmptyUtil.empty(blackUserId)) {
            return R.userError("blackUserId不能为空");
        }
        //判断是否已经拉黑过
        if(isBlackUserMember(blackUserId)){
            return R.userError("已经拉黑过");
        }
        //判断blackUserId是否和当前用户一样
        if(blackUserId.equals(UserSecurityUtil.getCurrUserId())){
            return R.userError("不能拉黑自己");
        }
        final BlackUserEntity blackUserEntity = new BlackUserEntity();
        blackUserEntity.setUserId(UserSecurityUtil.getCurrUserId());
        blackUserEntity.setBlackUserId(blackUserId);
        blackUserEntity.setCreateTime(new Date());
        blackUserEntity.setUpdateTime(new Date());
        final int addCount = blackUserDao.insert(blackUserEntity);
        if (addCount > 0) {
            redisTemplate.opsForSet().add("BLACK_USER_" + UserSecurityUtil.getCurrUserId(), blackUserId);
            return R.ok("拉黑成功");
        } else {
            return R.userError("不知道为何，拉黑失败");
        }
    }

    @Override
    public Set<String> getCurrUserBlackUserIds() {
        Set<String> blackUserIds = redisTemplate.opsForSet().members("BLACK_USER_" + UserSecurityUtil.getCurrUserId());
        return blackUserIds;
    }

    @Override
    public Set<String> getSpecifyUserBlackUserIds(String userId) {
        Set<String> blackUserIds = redisTemplate.opsForSet().members("BLACK_USER_" + userId);
        return blackUserIds;
    }

    @Override
    public Boolean isBlackUserMember(String userId) {
        Set<String> currUserBlackUserIds = getCurrUserBlackUserIds();
        return currUserBlackUserIds.contains(userId);
    }

    @Override
    public Boolean isBlackUserMember(String userId, String blackUserId) {
        Set<String> specifyUserBlackUserIds = getSpecifyUserBlackUserIds(userId);
        return specifyUserBlackUserIds.contains(blackUserId);
    }

    @Override
    public Long getUserBlackUserCount(String userId) {
        if(EmptyUtil.empty(userId)){
            userId = UserSecurityUtil.getCurrUserId();
        }
        Long count = redisTemplate.opsForSet().size("BLACK_USER_" + userId);
        return count;
    }


    private Page<UserEntity> blackUserEntityPageToUserEntityPage(Page<BlackUserEntity> blackUserEntityPage) {
        final Page<UserEntity> resPage = new Page<>();
        //判断传入的数据是否为空
        if (EmptyUtil.empty(blackUserEntityPage) || EmptyUtil.empty(blackUserEntityPage.getRecords())) {
            return resPage;
        }
        //拷贝原始数据
        BeanUtils.copyProperties(blackUserEntityPage,resPage );

        final List<String> blackUserIds = blackUserEntityPage.getRecords().stream().map(x -> {
            return x.getBlackUserId();
        }).collect(Collectors.toList());
        final R userEntitiesRes = userService.getUsersInfoByIds(blackUserIds);
        if (userEntitiesRes.getCode() == 0) {
            List<UserEntity> userEntities = (List<UserEntity>) userEntitiesRes.getData();
            resPage.setRecords(userEntities);
        }
        return resPage;
    }
}