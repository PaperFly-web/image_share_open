package com.paperfly.imageShare.controller.admin;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.OperationLogEntity;
import com.paperfly.imageShare.service.OperationLogService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("admin/operationlog")
public class OperationLogController {
    @Autowired
    private OperationLogService operationLogService;

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
    public R info(@PathVariable("id") Long id){
		OperationLogEntity operationLog = operationLogService.getById(id);

        return R.ok().put("operationLog", operationLog);
    }

    /**
     * 保存
     */
    @PostMapping("/")
    public R save(@RequestBody OperationLogEntity operationLog){
		operationLogService.save(operationLog);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/")
    public R update(@RequestBody OperationLogEntity operationLog){
		operationLogService.updateById(operationLog);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/")
    public R delete(@RequestBody Long[] ids){
		operationLogService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
