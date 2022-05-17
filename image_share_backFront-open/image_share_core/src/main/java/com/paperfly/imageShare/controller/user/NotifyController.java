package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.NotifyEntity;
import com.paperfly.imageShare.service.NotifyService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("notify")
public class NotifyController {
    @Autowired
    private NotifyService notifyService;

    /**
     * 获取当前用户，各个消息分类数量
     */
    @GetMapping("/categoryCount")
    @OperLog(operModule = "消息通知",operDesc = "获取当前用户，各个消息分类数量",operType = OperTypeConst.SELECT)
    public R categoryCount(){
        return notifyService.categoryCount();
    }
    /**
     * 获取点赞详情
     */
    @PostMapping("/thumbNotifyDetails")
    @OperLog(operModule = "消息通知",operDesc = "获取点赞详情",operType = OperTypeConst.SELECT)
    public R thumbNotifyDetails(@RequestBody Page<NotifyEntity> page){
        return notifyService.thumbNotifyDetails(page);
    }
    /**
     * 获取评论详情
     */
    @PostMapping("/postCommentNotifyDetails")
    @OperLog(operModule = "消息通知",operDesc = "获取评论详情",operType = OperTypeConst.SELECT)
    public R postCommentDetails(@RequestBody Page<NotifyEntity> page){
        return notifyService.postCommentDetails(page);
    }
    /**
     * 获取关注详情
     */
    @PostMapping("/focusNotifyDetails")
    @OperLog(operModule = "消息通知",operDesc = "获取关注详情",operType = OperTypeConst.SELECT)
    public R focusDetails(@RequestBody Page<NotifyEntity> page){
        return notifyService.focusDetails(page);
    }

    /**
     * 获取系统消息详情
     */
    @PostMapping("/systemMessageDetails")
    @OperLog(operModule = "消息通知",operDesc = "获取系统消息详情",operType = OperTypeConst.SELECT)
    public R systemMessageDetails(@RequestBody Page<NotifyEntity> page){
        return notifyService.systemMessageNotifyDetails(page);
    }

    /**
     * 点赞全部已读
     */
    @PostMapping("/thumbHaveRead")
    @OperLog(operModule = "消息通知",operDesc = "点赞全部已读",operType = OperTypeConst.UPDATE)
    public R thumbHaveRead(){
        return notifyService.thumbHaveReadAndNotify(null);
    }

    /**
     * 评论全部已读
     */
    @PostMapping("/postCommentHaveRead")
    @OperLog(operModule = "消息通知",operDesc = "评论全部已读",operType = OperTypeConst.UPDATE)
    public R postCommentHaveRead(){
        return notifyService.postCommentHaveReadAndNotify(null);
    }
    /**
     * 关注全部已读
     */
    @PostMapping("/focusHaveRead")
    @OperLog(operModule = "消息通知",operDesc = "关注全部已读",operType = OperTypeConst.UPDATE)
    public R focusHaveRead(){
        return notifyService.focusHaveReadAndNotify(null);
    }
    /**
     * 当前系统用户消息全部已读
     */
    @PostMapping("/systemMessageHaveRead")
    @OperLog(operModule = "消息通知",operDesc = "当前系统用户消息全部已读",operType = OperTypeConst.UPDATE)
    public R systemMessageHaveRead(){
        return notifyService.systemMessageHaveRead(null);
    }



    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    @OperLog(operModule = "消息通知",operDesc = "删除",operType = OperTypeConst.DELETE)
    public R deleteNotifyById(@PathVariable("id")String id){
		return notifyService.deleteNotifyById(id);
    }

}
