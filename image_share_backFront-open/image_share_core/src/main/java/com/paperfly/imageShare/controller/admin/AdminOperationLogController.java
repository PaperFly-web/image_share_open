package com.paperfly.imageShare.controller.admin;

import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.OperationLogEntity;
import com.paperfly.imageShare.entity.ReportEntity;
import com.paperfly.imageShare.service.ExcepLogService;
import com.paperfly.imageShare.service.LoginLogService;
import com.paperfly.imageShare.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/operationLog")
public class AdminOperationLogController {

    @Autowired
    OperationLogService operationLogService;

    /**
     * 获取所有操作日志
     * @param searchDTO
     * @return
     */
    @PostMapping("/getOperationLogs")
    public R getOperationLogs(@RequestBody PageSearchDTO<OperationLogEntity> searchDTO){
        return operationLogService.getOperationLogs(searchDTO);
    }

    /**
     * 删除操作日志
     * @param ids
     * @return
     */
    @DeleteMapping
    public R deleteOperationLogs(@RequestBody List<String> ids){
        return operationLogService.deleteOperationLogs(ids);
    }
}
