package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.RecommendedDao;
import com.paperfly.imageShare.entity.RecommendedEntity;
import com.paperfly.imageShare.service.RecommendedService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;


@Service("recommendedService")
@Transactional
@Slf4j
public class RecommendedServiceImpl extends ServiceImpl<RecommendedDao, RecommendedEntity> implements RecommendedService {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    RecommendedDao recommendedDao;

    private static Gson gson = new GsonBuilder().create();

    @Override
    public ListenableFuture add(RecommendedEntity recommendedEntity) {
        final String userId = recommendedEntity.getUserId();
        final String postId = recommendedEntity.getPostId();
        final Float score = recommendedEntity.getScore();
        String body = userId+","+postId+","+score;
        ListenableFuture future = kafkaTemplate.send("image_share_big_data", body);
        return future;
    }


}