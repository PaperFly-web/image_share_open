package com.paperfly.imageShare.dao;

import com.paperfly.imageShare.entity.ThumbEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Mapper
public interface ThumbDao extends BaseMapper<ThumbEntity> {
    //添加点赞
	Integer add(ThumbEntity thumb);
}
