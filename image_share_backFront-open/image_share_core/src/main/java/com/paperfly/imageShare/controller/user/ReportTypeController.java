package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.ReportTypeEntity;
import com.paperfly.imageShare.service.ReportTypeService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("reportType")
public class ReportTypeController {
    @Autowired
    private ReportTypeService reportTypeService;

    /**
     * 查询举报类别（目前只支持查询大分类）
     */
    @PostMapping("/{reportType}")
    @OperLog(operModule = "举报类别",operDesc = "查询举报类别",operType = OperTypeConst.SELECT)
    public R getReportType(@PathVariable("reportType")Integer reportType,@RequestBody Page<ReportTypeEntity> page){
        return reportTypeService.getReportType(reportType,page);
    }


}
