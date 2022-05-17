package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import com.paperfly.imageShare.service.PersonalMessageService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("personalmessage")
public class PersonalMessageController {
    @Autowired
    private PersonalMessageService personalMessageService;

    /**
     * 获取当前用户与被私信用户的历史私信消息
     */
    @PostMapping("/{toUserId}")
    @OperLog(operModule = "私信",operDesc = "获取当前用户与被私信用户的历史私信消息",operType = OperTypeConst.SELECT)
    public R getCurrUserToUserPersonalMessage(@RequestBody Page<PersonalMessageEntity> page,
                                         @PathVariable("toUserId")String toUserId){
        return personalMessageService.getCurrUserToUserPersonalMessage(page,toUserId);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @OperLog(operModule = "私信",operDesc = "信息",operType = OperTypeConst.SELECT)
    public R info(@PathVariable("id") Long id){
		PersonalMessageEntity personalMessage = personalMessageService.getById(id);

        return R.ok().put("personalMessage", personalMessage);
    }

    /**
     * 设置对某用户消息全部已读
     */
    @PutMapping("/haveRead/{toUserId}")
    @OperLog(operModule = "私信",operDesc = "设置对某用户消息全部已读",operType = OperTypeConst.UPDATE)
    public R haveRead(@PathVariable("toUserId")String toUserId){
		return personalMessageService.haveReadAndUpdateNotify(toUserId);
    }



}
