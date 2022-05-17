package com.paperfly.imageShare.dao;

import com.paperfly.imageShare.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    /**
     * 通过用户ID/昵称/姓名查询用户
     * @param param
     * @return
     */
    List<UserEntity> findUserByIdOrSnakeNameOrName(String param);
}
