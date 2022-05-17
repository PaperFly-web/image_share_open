package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.ViewDetailsEntity;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface ViewDetailsService extends IService<ViewDetailsEntity> {

    /**
     * 添加浏览详情
     * @param viewDetails
     * @return
     */
    R add(ViewDetailsEntity viewDetails);

    /**
     * 分页获取当前用户浏览详情
     * @param page
     * @return
     */
    R getPageViews(Page<ViewDetailsEntity> page);

    /**
     * 通过ID删除详情
     * @param id
     * @return
     */
    R deleteViewsById(String id);
}

