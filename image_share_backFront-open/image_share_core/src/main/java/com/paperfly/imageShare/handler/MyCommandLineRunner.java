package com.paperfly.imageShare.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.paperfly.imageShare.common.utils.baidu.GsonUtils;
import com.paperfly.imageShare.dto.DataSyncDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.DataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 数据同步处理
 */
//@Component
public class MyCommandLineRunner implements CommandLineRunner {

    @Autowired
    DataSyncService dataSyncService;

    @Override
    public void run(String... var1) throws Exception {
        System.out.println("This will be execute when the project was started!");
        dataSyncService.receivePostFromCanal();
    }

}