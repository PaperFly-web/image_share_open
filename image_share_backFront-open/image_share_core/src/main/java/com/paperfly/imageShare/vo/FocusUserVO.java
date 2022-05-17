package com.paperfly.imageShare.vo;

import com.paperfly.imageShare.entity.UserEntity;
import lombok.Data;

import java.util.Date;

@Data
public class FocusUserVO {
    /**
     * 关注列表的ID
     */
    private String id;

    /**
     * 当前登录的用户
     */
    private UserEntity currUser;

    /**
     * 其他用户
     */
    private UserEntity userTwo;


    /**
     * 关注时间
     */
    private Date focusTime;

    /**
     * 关注类型 0:什么都没关系， 1：我关注你，2：你关注我，3：互相关注
     */
    private Integer focusType;

}
