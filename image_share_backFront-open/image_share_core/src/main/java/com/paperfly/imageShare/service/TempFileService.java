package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.dao.TempFileDao;
import com.paperfly.imageShare.entity.TempFileEntity;

import java.util.List;
import java.util.Map;

/**
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-02-04 14:47:37
 */
public interface TempFileService extends IService<TempFileEntity> {
    /**
     * 获取当前时间 之前几小时 保存的文件
     * @param hours 时间  单位小时
     * @return
     */
    List<TempFileEntity> getHoursAgo(Integer hours);
}

