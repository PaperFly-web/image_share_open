package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.ViewDetailsEntity;
import com.paperfly.imageShare.service.ViewDetailsService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("viewdetails")
public class ViewDetailsController {
    @Autowired
    private ViewDetailsService viewDetailsService;

    /**
     * 添加浏览详情
     */
    @PostMapping
    @OperLog(operModule = "浏览详情",operDesc = "添加浏览详情",operType = OperTypeConst.ADD)
    public R add(@RequestBody ViewDetailsEntity viewDetails){
        return viewDetailsService.add(viewDetails);
    }


    /**
     * 分页获取当前用户浏览详情
     */
    @PostMapping("/getViews")
    @OperLog(operModule = "浏览详情",operDesc = "分页获取当前用户浏览详情",operType = OperTypeConst.SELECT)
    public R getPageViews(@RequestBody Page<ViewDetailsEntity> page){
		return viewDetailsService.getPageViews(page);
    }


    /**
     * 通过ID删除详情
     */
    @DeleteMapping("/{id}")
    @OperLog(operModule = "浏览详情",operDesc = "通过ID删除详情",operType = OperTypeConst.DELETE)
    public R deleteViewsById(@PathVariable("id")String id){
		return viewDetailsService.deleteViewsById(id);
    }

}
