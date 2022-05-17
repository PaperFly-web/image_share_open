package com.paperfly.imageShare.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.DataSyncDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.PostEntity;

import java.util.List;

/**
 * 数据同步服务
 */
public interface DataSyncService {

    /**
     * 从canal接收Post数据
     *
     * @return
     */
    void receivePostFromCanal() throws InterruptedException, InvalidProtocolBufferException;

    /**
     * 同步数据到ES
     *
     * @param dataSyncDTO
     * @return
     */
    R syncPostToEs(DataSyncDTO<PostEntity> dataSyncDTO);

    /**
     * 删除的同步
     * @param dataSyncDTO
     * @return
     */
    R syncDeletePostToEs(DataSyncDTO<PostEntity> dataSyncDTO);

    /**
     * 添加数据的同步
     * @param dataSyncDTO
     * @return
     */
    R syncAddPostToEs(DataSyncDTO<PostEntity> dataSyncDTO);

    /**
     *
     * 更新数据的同步
     * @param dataSyncDTO
     * @return
     */
    R syncUpdatePostToEs(DataSyncDTO<PostEntity> dataSyncDTO);

    /**
     * 把同步失败的数据添加到redis中，等稍后处理
     * @param dataSyncDTO
     */
    void addSyncErrToRedis(DataSyncDTO dataSyncDTO);

    /**
     * 获取同步错误的数据集合
     * @return
     */
    List<DataSyncDTO<PostEntity>> getSyncErr();

    /**
     * 移除Redis中的同步成功的
     * @param dataSyncDTO
     * @return
     */
    Boolean removeSyncRrrOnRedis(DataSyncDTO dataSyncDTO);
}
