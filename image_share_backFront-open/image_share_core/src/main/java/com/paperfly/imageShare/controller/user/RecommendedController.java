package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.RecommendedEntity;
import com.paperfly.imageShare.service.RecommendedService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("recommended")
public class RecommendedController {
    @Autowired
    private RecommendedService recommendedService;




    /**
     * 信息
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		RecommendedEntity recommended = recommendedService.getById(id);

        return R.ok().put("recommended", recommended);
    }

    *//**
     * 保存
     *//*
    @PostMapping("/")
    public R save(@RequestBody RecommendedEntity recommended){
		recommendedService.save(recommended);

        return R.ok();
    }

    *//**
     * 修改
     *//*
    @PutMapping("/")
    public R update(@RequestBody RecommendedEntity recommended){
		recommendedService.updateById(recommended);

        return R.ok();
    }

    *//**
     * 删除
     *//*
    @DeleteMapping("/")
    public R delete(@RequestBody Long[] ids){
		recommendedService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }*/

}
