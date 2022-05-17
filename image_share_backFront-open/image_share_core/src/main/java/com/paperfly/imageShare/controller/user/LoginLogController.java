package com.paperfly.imageShare.controller.user;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.LoginLogEntity;
import com.paperfly.imageShare.service.LoginLogService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
@RestController
@RequestMapping("loginlog")
public class LoginLogController {
    @Autowired
    private LoginLogService loginLogService;

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
		LoginLogEntity loginLog = loginLogService.getById(id);

        return R.ok().put("loginLog", loginLog);
    }

    /**
     * 保存
     */
    @PostMapping("/")
    public R save(@RequestBody LoginLogEntity loginLog){
		loginLogService.save(loginLog);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/")
    public R update(@RequestBody LoginLogEntity loginLog){
		loginLogService.updateById(loginLog);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/")
    public R delete(@RequestBody Long[] ids){
		loginLogService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
