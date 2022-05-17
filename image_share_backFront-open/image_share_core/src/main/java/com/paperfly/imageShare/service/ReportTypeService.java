package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.ReportTypeEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface ReportTypeService extends IService<ReportTypeEntity> {

    /**
     * 获取举报类别
     * @param page
     * @param reportType
     * @return
     */
    R getReportType(Integer reportType,Page<ReportTypeEntity> page);

    /**
     * 添加举报分类
     * @param reportType
     * @return
     */
    R addReportType(ReportTypeEntity reportType);

    /**
     * 获取举报类型
     * @param searchDTO
     * @return
     */
    R getReportTypes(PageSearchDTO<ReportTypeEntity> searchDTO);

    /**
     * 删除举报分类
     * @param reportType
     * @return
     */
    R deleteReportType(ReportTypeEntity reportType);


    R deleteManyReportType(List<String> ids);
}

