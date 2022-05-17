package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.MyJwtTokenUtil;
import com.paperfly.imageShare.dto.UserDTO;
import com.paperfly.imageShare.dto.UserInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.UserService;
import com.paperfly.imageShare.common.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;


/**
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-26 14:34:50
 */
@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 获取当前用户个人信息
     */
    @GetMapping
    @OperLog(operModule = "用户",operDesc = " 获取当前用户个人信息",operType = OperTypeConst.SELECT)
    public R getCurrUserInfo() {
        return userService.getUserInfo();
    }

    /**
     * 获取用户粉丝，帖子，关注，黑名单   数量
     *
     * @param userId
     * @return
     */
    @GetMapping("/getPostFansFocusCount/{userId}")
    @OperLog(operModule = "用户",operDesc = " 获取用户粉丝，帖子，关注，黑名单 数量",operType = OperTypeConst.SELECT)
    public R getPostFansFocusCount(@PathVariable("userId") String userId) {
        return userService.getPostFansFocusCount(userId);
    }


    /**
     * 获取指定用户信息
     */
    @GetMapping("/userInfo/{userId}")
    @OperLog(operModule = "用户",operDesc = "获取指定用户信息",operType = OperTypeConst.SELECT)
    public R getUserInfoById(@PathVariable("userId") String userId) {
        return userService.getUserInfoById(userId);
    }

    /**
     * 注册用户
     */
    @PostMapping
    @OperLog(operModule = "用户",operDesc = "注册用户",operType = OperTypeConst.ADD)
    public R save(@RequestBody UserDTO user) {
        return userService.register(user);
    }

    /* *//**
     * 修改
     *//*
    @PutMapping
    public R update(@RequestBody UserEntity user) {
        userService.updateById(user);

        return R.ok();
    }*/

    /**
     * 刷新token
     */
    @PutMapping("/refreshToken")
    @OperLog(operModule = "用户",operDesc = "刷新token",operType = OperTypeConst.UPDATE)
    public R refreshToken(HttpServletRequest request) {
        return userService.refreshToken(request);
    }

    /**
     * 账户注销
     */
    @DeleteMapping
    @OperLog(operModule = "用户",operDesc = "账户注销",operType = OperTypeConst.DELETE)
    public R accountCancellation(@RequestBody Map<String, String> param) {
        String captchaCode = param.get("captchaCode");
        return userService.accountCancellation(captchaCode);
    }

    /**
     * 更新用户头像
     *
     * @param headImage 头像文件
     * @return
     */
    @PutMapping("/updateHeadImage")
    @OperLog(operModule = "用户",operDesc = "更新用户头像",operType = OperTypeConst.UPDATE)
    public R updateHeadImage(@RequestParam("file") MultipartFile headImage) {
        return userService.updateHeadImage(headImage);
    }

    /**
     * 通过用户ID获取用户头像
     */
    @GetMapping("/headImg/{userId}")
    @OperLog(operModule = "用户",operDesc = "通过用户ID获取用户头像",operType = OperTypeConst.SELECT)
    public R getHeadImg(@PathVariable("userId") String userId) {
        return userService.getHeadImg(userId);
    }

    /**
     * 找回密码
     * email 用户在本站的邮箱
     * captchaCode 邮箱验证码
     * newPassword 新密码
     *
     * @return 找回结果
     */
    @PutMapping("/findPassword")
    @OperLog(operModule = "用户",operDesc = "找回密码",operType = OperTypeConst.UPDATE)
    public R findPassword(@RequestBody Map<String, String> findPwd) {
        String email = findPwd.get("email");
        String captchaCode = findPwd.get("emailCaptcha");
        String newPassword = findPwd.get("password");
        return userService.findPassword(email, captchaCode, newPassword);
    }

    @PutMapping("/updatePassword")
    @OperLog(operModule = "用户",operDesc = "修改密码",operType = OperTypeConst.UPDATE)
    public R updatePassword(@RequestBody Map<String, String> param) {
        String oldPassword = param.get("oldPassword");
        String newPassword = param.get("newPassword");
        return userService.updatePassword(oldPassword, newPassword);
    }

    @PutMapping("/updateEmail")
    @OperLog(operModule = "用户",operDesc = "修改用户邮箱",operType = OperTypeConst.UPDATE)
    public R updateEmail(@RequestBody Map<String, String> param) {
        String newEmail = param.get("newEmail");
        String emailCode = param.get("emailCode");
        return userService.updateEmail(newEmail, emailCode);
    }

    @PutMapping("/updateInfo")
    @OperLog(operModule = "用户",operDesc = "修改用户其他信息",operType = OperTypeConst.UPDATE)
    public R updateInfo(@RequestBody UserInfoDTO userInfoDTO) {
        return userService.updateInfo(userInfoDTO);
    }

    @PutMapping("/updateSnakeName")
    @OperLog(operModule = "用户",operDesc = "修改用户昵称",operType = OperTypeConst.UPDATE)
    public R updateSnakeName(@RequestBody Map<String, String> param) {
        String snakeName = param.get("snakeName");
        return userService.updateSnakeName(snakeName);
    }

    @GetMapping("/refreshHeadImage")
    @OperLog(operModule = "用户",operDesc = "刷新用户头像链接",operType = OperTypeConst.SELECT)
    public R refreshHeadImage() {
        return userService.refreshHeadImage();
    }
}
