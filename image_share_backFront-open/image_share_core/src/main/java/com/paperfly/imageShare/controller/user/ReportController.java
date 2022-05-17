package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.ReportEntity;
import com.paperfly.imageShare.service.ReportService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    /**
     * 添加举报
     */
    @PostMapping
    @OperLog(operModule = "举报",operDesc = "添加举报",operType = OperTypeConst.ADD)
    public R add(@RequestBody ReportEntity report){
        return reportService.add(report);
    }


}
