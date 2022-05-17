package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.RecommendedEntity;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface RecommendedService extends IService<RecommendedEntity> {

    /**
     * 往kafka中添加数据
     * @param recommendedEntity
     * @return
     */
    ListenableFuture add(RecommendedEntity recommendedEntity);


}

