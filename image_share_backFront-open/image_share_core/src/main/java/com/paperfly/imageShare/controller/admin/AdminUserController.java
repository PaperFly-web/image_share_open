package com.paperfly.imageShare.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/user")
public class AdminUserController {

    @Autowired
    UserService userService;

    /**
     * 通过用户ID/昵称/姓名查询用户
     * @param param
     * @return
     */
    @GetMapping("/{param}")
    public R findUserByIdOrSnakeNameOrName(String param){
        return userService.findUserByIdOrSnakeNameOrName(param);
    }

    /**
     * 批量获取系统用户
     * @param searchDTO
     * @return
     */
    @PostMapping("/getUsers")
    public R getUsers(@RequestBody PageSearchDTO<UserEntity> searchDTO){
        return userService.getUsers(searchDTO);
    }

    /**
     * 根据昵称或者邮箱搜索用户
     * @param keyWord
     * @return
     */
    @PostMapping("/findUserBySnakeNameOrEmail/{keyWord}")
    public R findUserBySnakeNameOrEmail(@PathVariable String keyWord){
        return userService.findUserBySnakeNameOrEmail(keyWord);
    }
    /**
     * 管理员修改用户信息
     * @param user
     * @return
     */
    @PutMapping("/updateUserInfo")
    public R updateUserInfo(@RequestBody UserEntity user){
        return userService.updateUserInfo(user);
    }

    /**
     * 禁止用户登录
     * @param userIds
     * @return
     */
    @PutMapping("/forbidUsers")
    public R forbidUsers(@RequestBody List<String> userIds){
        return userService.forbidUsers(userIds);
    }

    /**
     * 解开被禁止登录的用户
     * @param userIds
     * @return
     */
    @PutMapping("/unmakeUsers")
    public R unmakeUsers(@RequestBody List<String> userIds){
        return userService.unmakeUsers(userIds);
    }

    /**
     * 修改用户角色
     * @param userIds
     * @return
     */
    @PutMapping("/changeUsersRole/{role}")
    public R changeUsersRole(@PathVariable("role")Integer role,@RequestBody List<String> userIds){
        return userService.changeUsersRole(role,userIds);
    }

}
