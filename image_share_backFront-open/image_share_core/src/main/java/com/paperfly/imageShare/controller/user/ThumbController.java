package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.ThumbEntity;
import com.paperfly.imageShare.service.ThumbService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("thumb")
public class ThumbController {
    @Autowired
    private ThumbService thumbService;


    @GetMapping("/getCountByType/{toId}/{type}")
    @OperLog(operModule = "点赞",operDesc = "获取帖子或者评论点赞数量",operType = OperTypeConst.SELECT)
    public R getCountByType(@PathVariable("type")Integer type,@PathVariable("toId")String toId){
        return thumbService.getCountByType(toId,type);
    }

    @GetMapping("/currUserIsThumb/{toId}/{type}")
    @OperLog(operModule = "点赞",operDesc = "查询当前用户是否点击过指定帖子/评论",operType = OperTypeConst.SELECT)
    public R currUserIsThumb(@PathVariable("toId")String toId,@PathVariable("type")Integer type){
        return thumbService.currUserIsThumb(toId,type);
    }

    /**
     * 添加点赞
     */
    @PostMapping
    @OperLog(operModule = "点赞",operDesc = "添加点赞",operType = OperTypeConst.ADD)
    public R add(@RequestBody ThumbEntity thumb){
        return thumbService.add(thumb);
    }

    /**
     * 删除
     */
    @DeleteMapping
    @OperLog(operModule = "点赞",operDesc = "取消点赞",operType = OperTypeConst.DELETE)
    public R cancel(@RequestBody ThumbEntity thumb){
		return thumbService.cancel(thumb);
    }



}
