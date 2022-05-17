package com.paperfly.imageShare.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * 
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Mapper
public interface PersonalMessageDao extends BaseMapper<PersonalMessageEntity> {
	Integer add(PersonalMessageEntity personalMessageEntity);

	IPage<PersonalMessageEntity> getCurrUserToUserPersonalMessage(Page<PersonalMessageEntity> page,
																  @Param("el") PersonalMessageEntity personalMessageEntity);
}
