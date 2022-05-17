package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.FocusUserEntity;
import com.paperfly.imageShare.service.FocusUserService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("focususer")
public class FocusUserController {
    @Autowired
    private FocusUserService focusUserService;

    /**
     * 获取用户关注列表
     */
    @PostMapping("/getUserFocusPageById/{userId}")
    @OperLog(operModule = "关注",operDesc = "获取用户关注列表",operType = OperTypeConst.SELECT)
    public R getUserFocusPageById(@PathVariable("userId")String userId, @RequestBody Page<FocusUserEntity> page){
        return focusUserService.getUserFocusPageById(userId,page);
    }


    /**
     * 获取用户粉丝列表
     */
    @PostMapping("/getUserFansPageById/{userId}")
    @OperLog(operModule = "关注",operDesc = "获取用户粉丝列表",operType = OperTypeConst.SELECT)
    public R getUserFansPageById(@PathVariable("userId")String userId, @RequestBody Page<FocusUserEntity> page){
        return focusUserService.getUserFansPageById(userId,page);
    }



    /**
     * 判断当前用户是否关注指定用户
     */
    @GetMapping("/currUserIsFocusUser/{userId}")
    @OperLog(operModule = "关注",operDesc = "判断当前用户是否关注指定用户",operType = OperTypeConst.SELECT)
    public R currUserIsFocusUser(@PathVariable("userId")String userId){
        final String currUserId = UserSecurityUtil.getCurrUserId();
        return focusUserService.userIsFocus(currUserId,userId);
    }

    /**关注用户
     */
    @PostMapping("/{focusUserId}")
    @OperLog(operModule = "关注",operDesc = "关注用户",operType = OperTypeConst.ADD)
    public R focus(@PathVariable("focusUserId")String focusUserId){
		return focusUserService.focus(focusUserId);
    }



    /**
     * 取消关注
     */
    @DeleteMapping("/{focusUserId}")
    @OperLog(operModule = "关注",operDesc = "取消关注",operType = OperTypeConst.DELETE)
    public R cancel(@PathVariable("focusUserId")String focusUserId){
		return focusUserService.cancel(focusUserId);
    }

}
