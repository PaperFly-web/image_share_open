package com.paperfly.imageShare.monitor;

import com.google.common.collect.Lists;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.DataSyncDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.entity.TempFileEntity;
import com.paperfly.imageShare.service.DataSyncService;
import com.paperfly.imageShare.service.FileService;
import com.paperfly.imageShare.service.TempFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 每一小时扫描一下临时文件表中的文件，如果一小时后还未处理，则删除OSS中存储的
 */
@Service
@Slf4j
public class SyncMonitor {

    @Autowired
    DataSyncService dataSyncService;

    /**
     * 重试数据同步
     */
//    @Scheduled(fixedRate = 1000 * 30)
    public void reDataSync() {
        log.info("========================定时重试数据同步失败数据========================");
        final List<DataSyncDTO<PostEntity>> dataSyncErrList = dataSyncService.getSyncErr();
        for (DataSyncDTO<PostEntity> postEntityDataSyncDTO : dataSyncErrList) {
            final R r = dataSyncService.syncPostToEs(postEntityDataSyncDTO);
            if(r.getCode() == 0){
                dataSyncService.removeSyncRrrOnRedis(postEntityDataSyncDTO);
            }
        }
    }
}
