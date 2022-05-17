package com.paperfly.imageShare.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.TempFileDao;
import com.paperfly.imageShare.entity.TempFileEntity;
import com.paperfly.imageShare.service.TempFileService;
import org.springframework.transaction.annotation.Transactional;


@Service("tempFileService")
@Transactional
public class TempFileServiceImpl extends ServiceImpl<TempFileDao, TempFileEntity> implements TempFileService {

    @Autowired
    TempFileDao tempFileDao;


    @Override
    public List<TempFileEntity> getHoursAgo(Integer hours) {
        QueryWrapper<TempFileEntity> queryWrapper = new QueryWrapper<>();
        DateTime hoursAgo = DateUtil.offsetHour(new Date(), -hours);
        queryWrapper.le("update_time",hoursAgo);
        List<TempFileEntity> tempFileEntities = tempFileDao.selectList(queryWrapper);
        return tempFileEntities;
    }
}