package com.paperfly.imageShare.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.entity.ReportEntity;
import com.paperfly.imageShare.service.ReportService;
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
@RequestMapping("admin/report")
public class AdminReportController {
    @Autowired
    private ReportService reportService;


    /**
     * 批量处理举报
     */
    @PutMapping("/many/{isIll}")
    public R detailManyReports(@PathVariable("isIll")Integer isIll,@RequestBody List<String> ids){
        return reportService.detailManyReport(isIll,ids);
    }


    /**
     * 获取所有举报
     * @param searchDTO
     * @return
     */
    @PostMapping("/getReports")
    public R getReport(@RequestBody PageSearchDTO<ReportEntity> searchDTO){
        return reportService.getReports(searchDTO);
    }


    /**
     * 通知用户举报成功还是失败
     * @param report
     * @param isIll
     * @return
     */
    @PostMapping("/notifyUserReportState/{isIll}")
    public R addAndNotifyUserReportState(@PathVariable("isIll")String isIll,@RequestBody ReportEntity report){
        return reportService.addAndNotifyUserReportState(isIll,report);
    }
}
