package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.dto.PageSearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.BlackUserEntity;
import com.paperfly.imageShare.service.BlackUserService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("blackuser")
public class BlackUserController {
    @Autowired
    private BlackUserService blackUserService;

    /**
     * 查询当前用户的黑名单
     */
    @PostMapping("/getCurrUserBlackUser")
    @OperLog(operModule = "黑名单",operDesc = "获取当前用户黑名单",operType = OperTypeConst.SELECT)
    public R getCurrUserBlackUser(@RequestBody Page<BlackUserEntity> page){
        return blackUserService.getCurrUserBlackUser(page);
    }



    /**
     * 添加黑名单
     */
    @PostMapping("/{blackUserId}")
    @OperLog(operModule = "黑名单",operDesc = "给当前用户添加黑名单",operType = OperTypeConst.ADD)
    public R addBlackUser(@PathVariable("blackUserId")String blackUserId){
		return blackUserService.addCurrBlackUser(blackUserId);
    }


    /**
     * 当前用户移除黑名单
     * @param blackUserId 要删除的黑名单用户ID
     */
    @DeleteMapping("/{blackUserId}")
    @OperLog(operModule = "黑名单",operDesc = "删除当前用户黑名单",operType = OperTypeConst.DELETE)
    public R deleteCurrUserBlackUser(@PathVariable("blackUserId")String blackUserId){
		return blackUserService.deleteCurrUserBlackUser(blackUserId);
    }

}
