package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.ReportEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface ReportService extends IService<ReportEntity> {

    /**
     * 添加举报
     * @param report
     * @return
     */
    R add(ReportEntity report);



    /**
     * 批量处理举报
     * @param ids
     * @return
     */
    R detailManyReport(Integer isIll,List<String> ids);

    /**
     * 获取分类举报
     * @param reportType 举报类型   用户/帖子/评论
     * @param page
     * @return
    R getReportByType(Integer reportType,Integer state, Page<ReportEntity> page);*/

    /**
     * 获取所有举报
     * @param searchDTO
     * @return
     */
    R getReports(PageSearchDTO<ReportEntity> searchDTO);

    /**
     * 添加系统消息并通知
     * 管理员处理举报后，通知用户举报成功还是失败
     * @param report
     * @param isIll 1:举报成功，0或者其他举报失败
     * @return
     */
    R addAndNotifyUserReportState(String isIll,ReportEntity report);

    /**
     *根据举报处理状态获取
     * @param state
     * @param page
     * @return
    R getReportByState(Integer state, PageSearchDTO<ReportEntity> searchDTO);

    *//**
     * 根据用户邮箱，昵称查询举报内容
     * @param keyword
     * @param searchDTO
     * @return
     *//*
    R findPostByUserSnakeNameOrEmail(String keyword, PageSearchDTO<ReportEntity> searchDTO);*/
}

