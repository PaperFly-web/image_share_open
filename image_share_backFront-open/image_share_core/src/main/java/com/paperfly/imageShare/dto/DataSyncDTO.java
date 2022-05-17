package com.paperfly.imageShare.dto;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.paperfly.imageShare.entity.PostEntity;
import lombok.Data;

/**
 * 数据同步DTO
 */
@Data
public class DataSyncDTO<T> {
    /**
     * 之前数据
     */
    private T beforeData;
    /**
     * 之后数据
     */
    private T afterData;
    /**
     * 操作类型
     */
    private CanalEntry.EventType eventType;
}
