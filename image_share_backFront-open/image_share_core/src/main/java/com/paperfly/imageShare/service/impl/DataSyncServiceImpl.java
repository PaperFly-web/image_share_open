package com.paperfly.imageShare.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.ListUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.baidu.GsonUtils;
import com.paperfly.imageShare.dto.DataSyncDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.DataSyncService;
import com.paperfly.imageShare.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j(topic = "数据同步")
public class DataSyncServiceImpl implements DataSyncService {
    @Autowired
    ElasticsearchService elasticsearchService;

    @Value("${spring.elasticsearch.useIndex:}")
    String indexName;

    @Autowired
    RedisTemplate<String, String> redisTemplate;


    @Override
    public void receivePostFromCanal() throws InterruptedException, InvalidProtocolBufferException {
        //TODO 获取连接
        CanalConnector canalConnector = CanalConnectors.newSingleConnector
                (new InetSocketAddress("linux1", 11111), "example", "canal", "canal");

        while (true) {

            //TODO 连接
            canalConnector.connect();

            //TODO 订阅数据库
            canalConnector.subscribe("image_share.post");//订阅image_share的post表

            //TODO 获取数据（一次性获取100个数据）
            Message message = canalConnector.get(100);

            //TODO 获取Entry集合
            List<CanalEntry.Entry> entries = message.getEntries();

            //TODO 判断集合是否为空,如果为空,则等待一会继续拉取数据
            if (entries.size() <= 0) {
//                System.out.println("当次抓取没有数据，休息一会。。。。。。");
                Thread.sleep(3000);
            } else {

                //TODO 遍历entries，单条解析
                for (CanalEntry.Entry entry : entries) {
                    //1.获取表名
                    String tableName = entry.getHeader().getTableName();
                    //2.获取类型
                    CanalEntry.EntryType entryType = entry.getEntryType();
                    //3.获取序列化后的数据
                    ByteString storeValue = entry.getStoreValue();
                    //4.判断当前entryType类型是否为ROWDATA
                    if (CanalEntry.EntryType.ROWDATA.equals(entryType)) {
                        //5.反序列化数据
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                        //6.获取当前事件的操作类型
                        CanalEntry.EventType eventType = rowChange.getEventType();
                        //7.获取数据集
                        List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();
                        //8.遍历rowDataList，并打印数据集
                        for (CanalEntry.RowData rowData : rowDataList) {
                            JSONObject beforeData = new JSONObject();
                            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
                            for (CanalEntry.Column column : beforeColumnsList) {
                                beforeData.put(column.getName(), column.getValue());
                            }
                            JSONObject afterData = new JSONObject();
                            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                            for (CanalEntry.Column column : afterColumnsList) {
                                afterData.put(column.getName(), column.getValue());
                            }
                            //数据打印
                            log.info("Table:" + tableName +
                                    ",EventType:" + eventType +
                                    ",Before:" + beforeData +
                                    ",After:" + afterData);
                            PostEntity beforePostEntity = GsonUtils.fromJson(beforeData.toJSONString(), PostEntity.class, true);
                            PostEntity afterPostEntity = GsonUtils.fromJson(afterData.toJSONString(), PostEntity.class, true);
                            final DataSyncDTO<PostEntity> postEntityDataSyncDTO = new DataSyncDTO<>();
                            postEntityDataSyncDTO.setAfterData(afterPostEntity);
                            postEntityDataSyncDTO.setBeforeData(beforePostEntity);
                            postEntityDataSyncDTO.setEventType(eventType);
                            this.syncPostToEs(postEntityDataSyncDTO);
                        }
                    } else {
                        log.info("当前操作类型为：" + entryType);
                    }
                }
            }
        }
    }

    @Override
    public R syncPostToEs(DataSyncDTO<PostEntity> dataSyncDTO) {
        R r = R.error();
        switch (dataSyncDTO.getEventType()) {
            case DELETE:
                r = this.syncDeletePostToEs(dataSyncDTO);
                break;
            case UPDATE:
                r = this.syncUpdatePostToEs(dataSyncDTO);
                break;
            case INSERT:
                r = this.syncAddPostToEs(dataSyncDTO);
                break;
            default:
                log.warn("没有合适的同步类型被发现");
        }
        return r;
    }

    @Override
    public R syncDeletePostToEs(DataSyncDTO<PostEntity> dataSyncDTO) {
        R r = R.error();
        if (EmptyUtil.empty(dataSyncDTO)) {
            return R.error("数据为空");
        }
        if (dataSyncDTO.getEventType() != CanalEntry.EventType.DELETE) {
            return R.error("同步的数据类型不是删除");
        }
        final PostEntity deletePost = dataSyncDTO.getBeforeData();
        try {
            r = elasticsearchService.deleteDoc(indexName, deletePost.getId());
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            //同步失败，先放入redis缓存中
            this.addSyncErrToRedis(dataSyncDTO);
        }
        return r;
    }

    @Override
    public R syncAddPostToEs(DataSyncDTO<PostEntity> dataSyncDTO) {
        R r = R.error();
        if (EmptyUtil.empty(dataSyncDTO)) {
            return R.error("数据为空");
        }
        if (dataSyncDTO.getEventType() != CanalEntry.EventType.INSERT) {
            return R.error("同步的数据类型不是添加");
        }
        String docId = dataSyncDTO.getAfterData().getId();
        String afterPostJson = GsonUtils.toJson(dataSyncDTO.getAfterData(), true);
        Map map = GsonUtils.fromJson(afterPostJson, Map.class);
        String topic = (String) map.get("topic");
        List list = ListUtil.strToList(topic, " ");
        map.put("topic", list.toArray());
        try {
            r = elasticsearchService.addDoc(map, indexName, docId);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            //同步失败，先放入redis缓存中
            this.addSyncErrToRedis(dataSyncDTO);
        }
        return r;
    }

    @Override
    public R syncUpdatePostToEs(DataSyncDTO<PostEntity> dataSyncDTO) {
        R r = R.error();
        if (EmptyUtil.empty(dataSyncDTO)) {
            return R.error("数据为空");
        }
        if (dataSyncDTO.getEventType() != CanalEntry.EventType.UPDATE) {
            return R.error("同步的数据类型不是更新");
        }
        //如果是非法的帖子，或者是删除的帖子，就直接删除ES中的数据
        final Integer state = dataSyncDTO.getAfterData().getState();
        final Integer isDeleted = dataSyncDTO.getAfterData().getIsDeleted();
        if (state == 2 || state == 3 || isDeleted == 1) {
            dataSyncDTO.setEventType(CanalEntry.EventType.DELETE);
            return this.syncDeletePostToEs(dataSyncDTO);
        }
        String docId = dataSyncDTO.getBeforeData().getId();
        String afterPostJson = GsonUtils.toJson(dataSyncDTO.getAfterData(), true);
        Map map = GsonUtils.fromJson(afterPostJson, Map.class);
        String topic = (String) map.get("topic");
        List list = ListUtil.strToList(topic, " ");
        map.put("topic", list.toArray());

        try {
            r = elasticsearchService.updateDoc(map, indexName, docId);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            //同步失败，先放入redis缓存中
            this.addSyncErrToRedis(dataSyncDTO);
        }
        return r;
    }

    @Override
    public void addSyncErrToRedis(DataSyncDTO dataSyncDTO) {
        redisTemplate.opsForSet().add("SYNC_POST_ERROR", GsonUtils.toJson(dataSyncDTO));
    }

    @Override
    public List<DataSyncDTO<PostEntity>> getSyncErr() {
        Set<String> syncPostError = redisTemplate.opsForSet().members("SYNC_POST_ERROR");
        ArrayList<DataSyncDTO<PostEntity>> resList = new ArrayList<>();
        if (EmptyUtil.empty(syncPostError)) {
            return resList;
        }
        //把json字符串转换成DataSyncDTO
        for (String jsonSyncDTO : syncPostError) {
            DataSyncDTO dataSyncDTO = GsonUtils.fromJson(jsonSyncDTO, DataSyncDTO.class);
            //
            PostEntity beforePostEntity = GsonUtils.fromJson(GsonUtils.toJson(dataSyncDTO.getBeforeData()), PostEntity.class);
            PostEntity afterPostEntity = GsonUtils.fromJson(GsonUtils.toJson(dataSyncDTO.getAfterData()), PostEntity.class);
            dataSyncDTO.setBeforeData(beforePostEntity);
            dataSyncDTO.setAfterData(afterPostEntity);
            resList.add(dataSyncDTO);
        }
        return resList;
    }

    @Override
    public Boolean removeSyncRrrOnRedis(DataSyncDTO dataSyncDTO) {
        final Long removeNum = redisTemplate.opsForSet().remove("SYNC_POST_ERROR", GsonUtils.toJson(dataSyncDTO));
        return !EmptyUtil.empty(removeNum) && removeNum > 0;
    }


}
