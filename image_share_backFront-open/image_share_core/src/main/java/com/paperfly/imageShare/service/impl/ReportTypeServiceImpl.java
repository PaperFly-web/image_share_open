package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.ListUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.ReportEntity;
import com.paperfly.imageShare.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.ReportTypeDao;
import com.paperfly.imageShare.entity.ReportTypeEntity;
import com.paperfly.imageShare.service.ReportTypeService;
import org.springframework.transaction.annotation.Transactional;


@Service("reportTypeService")
@Transactional
public class ReportTypeServiceImpl extends ServiceImpl<ReportTypeDao, ReportTypeEntity> implements ReportTypeService {

    @Autowired
    ReportTypeDao reportTypeDao;

    @Override
    public R getReportType(Integer reportType,Page<ReportTypeEntity> page) {
        //目前支持查询父类举报类别
        final QueryWrapper<ReportTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("father_report_type_id");
        queryWrapper.eq("report_type",reportType);
        final List<ReportTypeEntity> resList = reportTypeDao.selectList(queryWrapper);
        return R.ok("查询成功").put("data",resList);
    }

    @Override
    public R addReportType(ReportTypeEntity reportType) {
        if(EmptyUtil.empty(reportType)){
            return R.userError("实体类为空");
        }
        if(EmptyUtil.empty(reportType.getReportTypeContent())){
            return R.userError("举报内容不能为空");
        }
        if(reportType.getReportTypeContent().length()>30){
            return R.userError("举报内容字数不能超过30个");
        }
        if(!ListUtil.contains(reportType.getReportType(),0,1,2)){
            return R.userError("举报分类错误");
        }
        //暂时只支持大类
        reportType.setFatherReportTypeId(null);
        reportType.setId(null);
        final int insertCount = reportTypeDao.insert(reportType);
        if(insertCount>0){
            return R.ok("添加举报分类成");
        }else {
            return R.error("添加举报分类失败");
        }
    }

    @Override
    public R getReportTypes(PageSearchDTO<ReportTypeEntity> searchDTO) {
        final QueryWrapper<ReportTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time",searchDTO.getStartTime());
        queryWrapper.le("create_time",searchDTO.getEndTime());

        //添加条件
        if(!EmptyUtil.empty(searchDTO.getCondition())){
            Map<String, Object> condition = searchDTO.getCondition();
            //判断条件是否有举报类别
            if(!EmptyUtil.empty(condition.get("report_type"))){
                queryWrapper.eq("report_type",condition.get("report_type"));
            }
            //判断条件是否有搜索的关键字，查询举报内容
            if(!EmptyUtil.empty(condition.get("keyword"))){
                queryWrapper.like("report_type_content",condition.get("keyword"));
            }
        }
        final Page<ReportTypeEntity> reportTypeEntityPage = reportTypeDao.selectPage(searchDTO.getPage(), queryWrapper);

        return R.ok("查询成功").put("data",reportTypeEntityPage);
    }

    @Override
    public R deleteReportType(ReportTypeEntity reportType) {
        if(EmptyUtil.empty(reportType)){
            return R.userError("实体类为空");
        }
        //检查举报ID
        if(EmptyUtil.empty(reportType.getId())){
            return R.userError("举报分类ID不能为空");
        }
        final UpdateWrapper<ReportTypeEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",reportType.getId()).or().eq("father_report_type_id",reportType.getId());
        final int deleteCount = reportTypeDao.delete(updateWrapper);
        if(deleteCount>0){
            return R.ok("删除举报分类成功");
        }else {
            return R.userError("删除举报分类失败，可能ID不存在");
        }
    }

    @Override
    public R deleteManyReportType(List<String> ids) {
        if(EmptyUtil.empty(ids)){
            return R.userError("ids为空");
        }
        final UpdateWrapper<ReportTypeEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",ids).or().in("father_report_type_id",ids);
        final int deleteCount = reportTypeDao.delete(updateWrapper);
        if(deleteCount>0){
            return R.ok("删除举报分类成功");
        }else {
            return R.userError("删除举报分类失败，可能ID不存在");
        }
    }
}