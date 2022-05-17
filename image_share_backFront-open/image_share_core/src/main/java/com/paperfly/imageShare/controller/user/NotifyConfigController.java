package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.NotifyConfigEntity;
import com.paperfly.imageShare.service.NotifyConfigService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("notifyConfig")
public class NotifyConfigController {
    @Autowired
    private NotifyConfigService notifyConfigService;

    /**
     * 获取当前用户的消息配置
     */
    @GetMapping
    @OperLog(operModule = "消息配置",operDesc = "获取当前用户的消息配置",operType = OperTypeConst.SELECT)
    public R getCurrUserNotifyConfig(){
        return notifyConfigService.getCurrUserNotifyConfig();
    }




    /**
     * 更新消息配置
     */
    @PutMapping
    @OperLog(operModule = "消息配置",operDesc = "更新消息配置",operType = OperTypeConst.UPDATE)
    public R update(@RequestBody NotifyConfigEntity notifyConfig){
        return notifyConfigService.updateNotifyConfig(notifyConfig);
    }

}
