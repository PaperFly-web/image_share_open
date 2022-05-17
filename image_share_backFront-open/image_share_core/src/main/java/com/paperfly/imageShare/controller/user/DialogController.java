package com.paperfly.imageShare.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.DialogDTO;
import com.paperfly.imageShare.entity.DialogEntity;
import com.paperfly.imageShare.entity.NotifyEntity;
import com.paperfly.imageShare.service.DialogService;
import com.paperfly.imageShare.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("dialog")
public class DialogController {
    @Autowired
    private DialogService dialogService;

    /**
     * 创建会话
     */
    @PostMapping
    @OperLog(operModule = "会话",operDesc = "创建会话",operType = OperTypeConst.ADD)
    public R create(@RequestBody DialogDTO dialogDTO){
        return dialogService.create(dialogDTO);
    }

    /**
     * 创建双会话
     */
    @PostMapping("/createUserAndToUserDialog")
    @OperLog(operModule = "会话",operDesc = "创建双方会话",operType = OperTypeConst.ADD)
    public R createUserAndToUserDialog(@RequestBody DialogDTO dialogDTO){
        return dialogService.createUserAndToUserDialog(dialogDTO);
    }

    /**
     * 查询当前用户的会话
     */
    @PostMapping("/getCurrUserDialog")
    @OperLog(operModule = "会话",operDesc = "查询当前用户的会话",operType = OperTypeConst.SELECT)
    public R getCurrUserDialogPage(@RequestBody Page<DialogEntity> page){
        return dialogService.getCurrUserDialogPage(page);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{toUserId}")
    @OperLog(operModule = "会话",operDesc = "删除会话",operType = OperTypeConst.DELETE)
    public R delete(@PathVariable("toUserId")String toUserId){
        return dialogService.delete(toUserId);
    }

    /**
     * 获取和指定用户的会话
     */
    @GetMapping("/{toUserId}")
    @OperLog(operModule = "会话",operDesc = "获取指定用户会话",operType = OperTypeConst.SELECT)
    public R getDialogByToUserId(@PathVariable("toUserId")String toUserId){
        return dialogService.getDialogByToUserId(toUserId);
    }
}
