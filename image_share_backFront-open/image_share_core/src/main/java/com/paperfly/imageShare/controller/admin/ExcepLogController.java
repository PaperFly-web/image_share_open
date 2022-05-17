package com.paperfly.imageShare.controller.admin;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.ExcepLogEntity;
import com.paperfly.imageShare.service.ExcepLogService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-02-01 14:21:07
 */
@RestController
@RequestMapping("admin/exceplog")
public class ExcepLogController {
    @Autowired
    private ExcepLogService excepLogService;

    /**
     * 列表
     */
    @RequestMapping("/")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") String id){
		ExcepLogEntity excepLog = excepLogService.getById(id);

        return R.ok().put("excepLog", excepLog);
    }

    /**
     * 保存
     */
    @PostMapping("/")
    public R save(@RequestBody ExcepLogEntity excepLog){
		excepLogService.save(excepLog);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/")
    public R update(@RequestBody ExcepLogEntity excepLog){
		excepLogService.updateById(excepLog);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/")
    public R delete(@RequestBody String[] ids){
		excepLogService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
