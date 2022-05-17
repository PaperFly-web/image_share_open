package com.paperfly.imageShare.monitor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.paperfly.imageShare.entity.TempFileEntity;
import com.paperfly.imageShare.service.FileService;
import com.paperfly.imageShare.service.TempFileService;
import com.paperfly.imageShare.service.impl.AliYunFileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ListUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每一小时扫描一下临时文件表中的文件，如果一小时后还未处理，则删除OSS中存储的
 */
@Service
@Slf4j
public class FileMonitor {

    @Autowired
    TempFileService tempFileService;

    @Autowired
    @Qualifier("aliYunFileService")
    FileService aliYunFileService;

    /**
     * 每小时执行一次，清理临时文件
     */
//    @Scheduled(cron = "0 0 */1 * * ?")
//    @Scheduled(fixedRate = 1000 * 30)
    public void deleteExpireTempFile() {
        log.info("========================定时清理临时文件========================");
        List<TempFileEntity> hoursAgoTempFiles = tempFileService.getHoursAgo(1);
        //如果文件大于1000个，则拆分多份(因为阿里云OSS一次最多只能删除1000个文件)
        if (hoursAgoTempFiles.size() > 1000) {
            List<List<TempFileEntity>> partition = Lists.partition(hoursAgoTempFiles, 1000);
            for (List<TempFileEntity> tempFileEntities : partition) {
                List<String> tempFileNames = tempFileEntities.stream().map(x -> x.getObjectName()).collect(Collectors.toList());
                aliYunFileService.deleteFiles(tempFileNames);
            }
        } else {
            List<String> tempFileNames = hoursAgoTempFiles.stream().map(x -> x.getObjectName()).collect(Collectors.toList());
            aliYunFileService.deleteFiles(tempFileNames);

        }
        List<String> temFileIds = hoursAgoTempFiles.stream().map(tempFileEntity -> tempFileEntity.getId())
                .collect(Collectors.toList());
        tempFileService.removeByIds(temFileIds);
    }
}
