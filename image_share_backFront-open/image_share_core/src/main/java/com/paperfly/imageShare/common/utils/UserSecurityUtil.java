package com.paperfly.imageShare.common.utils;

import com.paperfly.imageShare.entity.UserEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserSecurityUtil {

    /**
     * 获取当前用户邮箱
     * @return
     */
    public static String getCurrUsername(){
        if(EmptyUtil.empty(SecurityContextHolder.getContext())){
            return "anonymous@paperfly.com";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!EmptyUtil.empty(authentication) && !(authentication instanceof AnonymousAuthenticationToken)) {
            final UserEntity user = (UserEntity)authentication.getPrincipal();
            String currentUserName = user.getEmail();
            return currentUserName;
        }else{
            return "anonymous@paperfly.com";
        }
    }

    /**
     * 获取当前用户ID
     * @return
     */
    public static String getCurrUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!EmptyUtil.empty(authentication) && !(authentication instanceof AnonymousAuthenticationToken)) {
            final UserEntity user = (UserEntity)authentication.getPrincipal();
            String userId = user.getId();
            return userId;
        }else{
            return "";
        }
    }

    /**
     * 获取当前用户ID
     * @return
     */
    public static String getCurrUserSnakeName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!EmptyUtil.empty(authentication) && !(authentication instanceof AnonymousAuthenticationToken)) {
            final UserEntity user = (UserEntity)authentication.getPrincipal();
            String snakeName = user.getSnakeName();
            return snakeName;
        }else{
            return "";
        }
    }

    /**
     * 获取当前用户角色
     * @return
     */
    public static Integer getCurrUserRole(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!EmptyUtil.empty(authentication) && !(authentication instanceof AnonymousAuthenticationToken)) {

            final UserEntity user = (UserEntity)authentication.getPrincipal();
            final Integer role = user.getRole();
            return role;
        }else{
            return 0;
        }
    }

    /**
     * 获取当前用户token
     * @return
     */
    public static String getCurrUserToken(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!EmptyUtil.empty(authentication) && !(authentication instanceof AnonymousAuthenticationToken)) {
            final String token = (String)authentication.getCredentials();
            return token;
        }else{
            return "";
        }
    }
}
