package com.paperfly.imageShare.controller.admin;

import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.ReportTypeEntity;
import com.paperfly.imageShare.service.ReportTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("admin/reportType")
public class AdminReportTypeController {
    @Autowired
    private ReportTypeService reportTypeService;


    /**
     * 添加举报分类
     */
    @PostMapping
    public R addReportType(@RequestBody ReportTypeEntity reportType){
        return reportTypeService.addReportType(reportType);
    }

    /**
     * 删除举报举报分类
     */
    @DeleteMapping
    public R deleteReportType(@RequestBody ReportTypeEntity reportType){
        return reportTypeService.deleteReportType(reportType);
    }

    /**
     * 批量删除举报举报分类
     */
    @DeleteMapping("/many")
    public R deleteManyReportType(@RequestBody List<String> ids){
        return reportTypeService.deleteManyReportType(ids);
    }
    /**
     * 获取举报类型
     */
    @PostMapping("/getReportTypes")
    public R getReportTypes(@RequestBody PageSearchDTO<ReportTypeEntity> searchDTO){
        return reportTypeService.getReportTypes(searchDTO);
    }

}
