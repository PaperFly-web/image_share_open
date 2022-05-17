package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.dto.UserDTO;
import com.paperfly.imageShare.dto.UserInfoDTO;
import com.paperfly.imageShare.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-26 14:34:50
 */
public interface UserService extends IService<UserEntity> {

    UserEntity login(String email);

    R refreshToken(HttpServletRequest request);

    R register(UserDTO user);

    /**
     * 账户注销，注销账户需要提供验证码
     * @param captchaCode 验证码
     * @return 注销结果
     */
    R accountCancellation(String captchaCode);

    /**
     * 修改密码
     * @param oldPassword 老密码
     * @param newPassword 新密码
     * @return 密码修改结果
     */
    R updatePassword(String oldPassword,String newPassword);

    /**
     * 修改用户邮箱
     * @param newEmail 新的邮箱
     * @param emailCode 修改邮箱验证码
     * @return 邮箱修改结果
     */
    R updateEmail(String newEmail,String emailCode);

    /**
     * 修改用户其他信息
     * @param userInfoDTO （姓名，个性签名，性别）
     * @return 返回修改结果
     */
    R updateInfo(UserInfoDTO userInfoDTO);

    /**
     * 修改用户头像
     * @param headImage 用户头像流
     * @return 返回修改结果
     */
    R updateHeadImage(MultipartFile headImage);

    /**
     * 修改用户昵称
     * @param snakeName 新的昵称名字
     * @return 更新结果
     */
    R updateSnakeName(String snakeName);

    /**
     * 找回密码
     * @param captchaCode 邮箱验证码
     * @return 找回结果
     */
    R findPassword(String email,String captchaCode,String newPassword);

    /**
     * 获取用户数据
     * @return 返回查询结果
     */
    R getUserInfo();

    /**
     * 刷新用户头像链接
     * @return
     */
    R refreshHeadImage();

    /**
     * 检查用户email是否存在
     * @param email
     * @return
     */
    Boolean checkEmailIsExist(String email);

    /**
     * 检查snakename是否已被注册
     * @param snakeName
     * @return
     */
    Boolean checkSnakeNameIsExist(String snakeName);

    /**
     * 检查token是否有效
     * @param token
     * @return
     */
    Boolean checkTokenIsValid(String token);

    /**
     * 获取指定用户信息
     * @param userId
     * @return
     */
    R getUserInfoById(String userId);

    /**
     * 通过用户ID获取用户头像
     * @param userId
     * @return
     */
    R getHeadImg(String userId);

    /**
     * 获取用户帖子，关注，粉丝  数量
     * @param userId
     * @return
     */
    R getPostFansFocusCount(String userId);

    /**
     * 批量获取用户信息,不管是否禁止登录等，除非删除，都可以获取到
     * @param userIds
     * @return
     */
    R getUsersInfoByIds(List<String> userIds);

    /**
     * 通过用户ID/昵称/姓名查询用户
     * @param param
     * @return
     */
    R findUserByIdOrSnakeNameOrName(String param);

    /**
     * 批量获取系统用户
     * @param searchDTO
     * @return
     */
    R getUsers(PageSearchDTO<UserEntity> searchDTO);

    /**
     * 管理员修改用户信息
     * @param user
     * @return
     */
    R updateUserInfo(UserEntity user);

    /**
     * 禁止用户登录
     * @param userIds
     * @return
     */
    R forbidUsers(List<String> userIds);

    /**
     * 检查用户是否被禁止登录
     * @param id
     * @return
     */
    boolean userIsForbid(String id);

    /**
     * 解开被禁止登录的用户
     * @param userIds
     * @return
     */
    R unmakeUsers(List<String> userIds);

    /**
     * 根据昵称或者邮箱搜索用户
     * @param keyWord
     * @return
     */
    R findUserBySnakeNameOrEmail(String keyWord);

    /**
     * 修改用户角色
     * @param role
     * @param userIds
     * @return
     */
    R changeUsersRole(Integer role, List<String> userIds);

    /**
     * 通过用户邮箱删除用户token
     * @param userEmail
     * @return
     */
    Long deleteUserTokenByEmail(String userEmail);
}

