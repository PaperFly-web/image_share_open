package com.paperfly.imageShare.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.common.constant.DialogConst;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.dao.DialogDao;
import com.paperfly.imageShare.dto.DialogDTO;
import com.paperfly.imageShare.entity.DialogEntity;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.DialogService;
import com.paperfly.imageShare.service.FileService;
import com.paperfly.imageShare.service.PersonalMessageService;
import com.paperfly.imageShare.service.UserService;
import com.paperfly.imageShare.vo.DialogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("dialogService")
@Transactional
@Slf4j
public class DialogServiceImpl extends ServiceImpl<DialogDao, DialogEntity> implements DialogService {
    @Autowired
    DialogDao dialogDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserService userService;

    @Autowired
    FileService fileService;

    @Autowired
    PersonalMessageService personalMessageService;

    /**
     * redis存储创建的会话   key
     */
    public static final String REDIS_DIALOG_PREFIX = "DIALOG";

    @Override
    public R create(DialogDTO dialogDTO) {
        //1.检查
        if (EmptyUtil.empty(dialogDTO)) {
            return R.userError("消息体为空");
        }

        //检查type类型
        if (!isType(dialogDTO.getType())) {
            return R.userError("没有这种会话类型type=【" + dialogDTO.getType() + "】");
        }
        //会话必须要有对方用户ID
        if (EmptyUtil.empty(dialogDTO.getToUserId())) {
            return R.userError("对话用户ID不能为空");
        }

        //判断是不是系统通知
        if (DialogConst.Type.SYSTEM_NOTIFY.getType().equals(dialogDTO.getType())) {
            //系统通知用户ID为  -1
            dialogDTO.setUserId("-1");
        }
        //userId不能为空
        if (EmptyUtil.empty(dialogDTO.getUserId())) {
            return R.userError("userId不能为空");
        }
        //判断userId与toUserId是否相等
        if (dialogDTO.getUserId().equals(dialogDTO.getToUserId())) {
            return R.userError("userId与toUserId不能相等");
        }


        dialogDTO.setCreateTime(new Date());
        dialogDTO.setUpdateTime(new Date());

        //判断是否已经创建过
        if (isCreate(dialogDTO)) {
            return R.ok("当前会话已经创建过");
        } else {
            try {
                final int insertCount = dialogDao.insert(dialogDTO.genEntity());
                if (insertCount > 0) {
                    //添加会话
                    redisTemplate.opsForSet().add(REDIS_DIALOG_PREFIX,
                            this.getRedisDialogStr(dialogDTO.getUserId(), dialogDTO.getToUserId()));
                    return R.ok("会话创建成功");
                } else {
                    return R.error("不知道为何，会话创建失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return R.error(e.getMessage());
            }
        }
    }

    @Override
    public R createUserAndToUserDialog(DialogDTO dialogDTO) {
        String userId = dialogDTO.getUserId();
        String toUserId = dialogDTO.getToUserId();
        Integer type = dialogDTO.getType();
        DialogDTO tmpDialog = new DialogDTO();
        R r1 = this.create(dialogDTO);

        tmpDialog.setType(type);
        tmpDialog.setUserId(toUserId);
        tmpDialog.setToUserId(userId);
        R r2 = this.create(tmpDialog);
        if (r1.getCode() != 0 || r2.getCode() != 0) {
            log.error("创建双会话出现错误,r1:{}，r2:{}", r1.getMsg(), r2.getMsg());
            throw new RuntimeException("创建双会话出现错误");
        }
        return R.ok("创建双会话成功");
    }

    @Override
    public Boolean isCreate(DialogDTO dialogDTO) {
        if (EmptyUtil.empty(dialogDTO)) {
            return false;
        }
        if (EmptyUtil.empty(dialogDTO.getToUserId()) || EmptyUtil.empty(dialogDTO.getUserId())) {
            return false;
        }
        final Boolean isCreate = redisTemplate.opsForSet().isMember(REDIS_DIALOG_PREFIX,
                this.getRedisDialogStr(dialogDTO.getUserId(), dialogDTO.getToUserId()));
        return isCreate;
    }

    @Override
    public R getCurrUserDialogPage(Page<DialogEntity> page) {
        final IPage<DialogEntity> resPage = dialogDao.getCurrUserDialogPage(UserSecurityUtil.getCurrUserId(), page);
        final List<String> toUserIds = resPage.getRecords().stream().map(x -> {
            return x.getToUserId();
        }).collect(Collectors.toList());

        final HashMap<String, Date> dialogUserIdAndTime = new HashMap<>();
        for (DialogEntity record : resPage.getRecords()) {
            dialogUserIdAndTime.put(record.getToUserId(), record.getUpdateTime());
        }
        //获取对方对话用户信息
        final R resUsersInfo = userService.getUsersInfoByIds(toUserIds);
        final Page<DialogVO> finallyResPage = new Page<>();
        BeanUtils.copyProperties(resPage, finallyResPage);
        List<UserEntity> userEntities = new ArrayList<>();
        if(resUsersInfo.getData() instanceof List){
            userEntities = (List<UserEntity>) resUsersInfo.getData();
        }

        //由于是查询用户表的信息，所以updateTime为空，需要用对话表的updateTime补上
        for (UserEntity datum : userEntities) {
            datum.setUpdateTime(dialogUserIdAndTime.get(datum.getId()));
        }
        //按照updateTime降序排序
        Collections.sort(userEntities,
                (one, another) -> DateUtil.compare(another.getUpdateTime(), one.getUpdateTime()));

        final List<DialogVO> dialogVOS = userEntities.stream().map(x -> {
            final DialogVO dialogVO = new DialogVO();
            BeanUtils.copyProperties(x, dialogVO);
            dialogVO.setNoReadCount(personalMessageService.getNoReadCount(x.getId()));
            return dialogVO;
        }).collect(Collectors.toList());
        finallyResPage.setRecords(dialogVOS);
        return R.ok("查询成功").put("data", finallyResPage);
    }

    @Override
    public R delete(String toUserId) {
        if (EmptyUtil.empty(toUserId)) {
            return R.userError("删除失败，toUserId为空");
        }
        final DialogDTO dialogDTO = new DialogDTO();
        dialogDTO.setToUserId(toUserId);
        dialogDTO.setUserId(UserSecurityUtil.getCurrUserId());
        final Integer deleteCount = dialogDao.delete(dialogDTO);
        if (deleteCount > 0) {
            //移除会话
            redisTemplate.opsForSet().remove(REDIS_DIALOG_PREFIX,
                    getRedisDialogStr(UserSecurityUtil.getCurrUserId(), toUserId));
            return R.ok("删除会话成功");
        } else {
            return R.userError("删除会话失败,数据库没有这个会话");
        }
    }

    @Override
    public R updateDoubleDialogUpdateTime(String userId, String toUserId) {
        final Integer updateCount = dialogDao.updateDoubleDialogUpdateTime(userId, toUserId, new Date());
        return R.ok("更新会话时间成功").put("data", updateCount);
    }

    @Override
    public R getDialogByToUserId(String toUserId) {
        final QueryWrapper<DialogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",UserSecurityUtil.getCurrUserId());
        queryWrapper.eq("to_user_id",toUserId);
        queryWrapper.eq("type",0);
        final DialogEntity dialogEntity = dialogDao.selectOne(queryWrapper);
        if(EmptyUtil.empty(dialogEntity)){
            return R.userError("查询失败,没有与【"+toUserId+"】的会话");
        }else {
            final QueryWrapper<UserEntity> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.eq("id",toUserId);
            userInfoQueryWrapper.select("id","snake_name","username","head_image");
            final UserEntity userInfo = userService.getOne(userInfoQueryWrapper);
            userInfo.setUpdateTime(dialogEntity.getUpdateTime());
            userInfo.setHeadImage(fileService.getFileUrl(userInfo.getHeadImage(),false));
            final DialogVO dialogVO = new DialogVO();
            BeanUtils.copyProperties(userInfo,dialogVO);
            dialogVO.setNoReadCount(personalMessageService.getNoReadCount(dialogVO.getId()));
            return R.ok("查询成功").put("data", dialogVO);
        }
    }

    /**
     * 判断是否为会话指定的类型
     *
     * @param type
     * @return
     */
    public boolean isType(Integer type) {
        if (EmptyUtil.empty(type)) {
            return false;
        }
        for (DialogConst.Type value : DialogConst.Type.values()) {
            if (value.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取存储在redis的会话存储格式
     *
     * @param userId
     * @param toUserId
     * @return
     */
    private String getRedisDialogStr(String userId, String toUserId) {
        return userId + ":" + toUserId;
    }
}
