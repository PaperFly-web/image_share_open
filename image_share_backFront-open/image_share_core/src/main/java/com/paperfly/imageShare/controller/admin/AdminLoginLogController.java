package com.paperfly.imageShare.controller.admin;

import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.LoginLogEntity;
import com.paperfly.imageShare.entity.OperationLogEntity;
import com.paperfly.imageShare.service.ExcepLogService;
import com.paperfly.imageShare.service.LoginLogService;
import com.paperfly.imageShare.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/loginLog")
public class AdminLoginLogController {

    @Autowired
    LoginLogService loginLogService;

    /**
     * 获取所有登录日志
     * @param searchDTO
     * @return
     */
    @PostMapping("/getLoginLogs")
    public R getLoginLogs(@RequestBody PageSearchDTO<LoginLogEntity> searchDTO){
        return loginLogService.getLoginLogs(searchDTO);
    }

    /**
     * 删除登录日志
     * @param ids
     * @return
     */
    @DeleteMapping
    public R deleteLoginLogs(@RequestBody List<String> ids){
        return loginLogService.deleteLoginLogs(ids);
    }

}
