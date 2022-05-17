package com.paperfly.imageShare.controller.admin;

import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.ExcepLogEntity;
import com.paperfly.imageShare.entity.OperationLogEntity;
import com.paperfly.imageShare.service.ExcepLogService;
import com.paperfly.imageShare.service.LoginLogService;
import com.paperfly.imageShare.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/excepLog")
public class AdminExceptionLogController {

    @Autowired
    ExcepLogService excepLogService;

    /**
     * 获取所有异常日志
     * @param searchDTO
     * @return
     */
    @PostMapping("/getExcepLogs")
    public R getExcepLogs(@RequestBody PageSearchDTO<ExcepLogEntity> searchDTO){
        return excepLogService.getExcepLogs(searchDTO);
    }

    /**
     * 删除异常日志
     * @param ids
     * @return
     */
    @DeleteMapping
    public R deleteExcepLogs(@RequestBody List<String> ids){
        return excepLogService.deleteExcepLogs(ids);
    }

}
