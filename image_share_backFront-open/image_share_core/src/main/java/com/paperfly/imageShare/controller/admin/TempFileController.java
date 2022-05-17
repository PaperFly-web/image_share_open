package com.paperfly.imageShare.controller.admin;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paperfly.imageShare.entity.TempFileEntity;
import com.paperfly.imageShare.service.TempFileService;
import com.paperfly.imageShare.common.utils.R;



/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-02-04 14:47:37
 */
@RestController
@RequestMapping("admin/imageShare/tempfile")
public class TempFileController {
    @Autowired
    private TempFileService tempFileService;

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
		TempFileEntity tempFile = tempFileService.getById(id);

        return R.ok().put("tempFile", tempFile);
    }

    /**
     * 保存
     */
    @PostMapping("/")
    public R save(@RequestBody TempFileEntity tempFile){
		tempFileService.save(tempFile);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/")
    public R update(@RequestBody TempFileEntity tempFile){
		tempFileService.updateById(tempFile);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/")
    public R delete(@RequestBody String[] ids){
		tempFileService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
