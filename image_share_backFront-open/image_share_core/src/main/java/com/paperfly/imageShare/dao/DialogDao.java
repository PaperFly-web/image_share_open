package com.paperfly.imageShare.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.dto.DialogDTO;
import com.paperfly.imageShare.entity.DialogEntity;
import com.paperfly.imageShare.entity.NotifyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.xml.crypto.Data;
import java.util.Date;

/**
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@Mapper
public interface DialogDao extends BaseMapper<DialogEntity> {

    Integer isCreate(DialogDTO dialogDTO);

    /**
     * 删除会话
     *
     * @param dialogDTO
     * @return
     */
    Integer delete(DialogDTO dialogDTO);

    IPage<DialogEntity> getCurrUserDialogPage(String userId, Page<DialogEntity> page);

    Integer updateDoubleDialogUpdateTime(@Param("userId") String userId,
                                         @Param("toUserId") String toUserId,
                                         @Param("updateTime") Date updateTime);
}
