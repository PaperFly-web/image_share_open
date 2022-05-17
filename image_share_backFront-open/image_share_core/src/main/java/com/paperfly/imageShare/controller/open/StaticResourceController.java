package com.paperfly.imageShare.controller.open;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.service.StaticResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("open/static")
public class StaticResourceController {
    @Autowired
    StaticResourceService staticResourceService;

    @GetMapping("/loginBg/{fileName}")
    @OperLog(operModule = "静态资源",operDesc = "获取指定登录背景图",operType = OperTypeConst.SELECT)
    public R getLoginBgImg(@PathVariable("fileName") String fileName){
        return staticResourceService.getLoginBgImg(fileName);
    }

    @GetMapping("/loginBg")
    @OperLog(operModule = "静态资源",operDesc = "获取所有登录背景图",operType = OperTypeConst.SELECT)
    public R getLoginBgImg(){
        return staticResourceService.getLoginBgImg();
    }

    @GetMapping("/loginBg/oneRandom")
    @OperLog(operModule = "静态资源",operDesc = "随机获取一张背景图",operType = OperTypeConst.SELECT)
    public R getLoginBgImgByOneRandom(){
        return staticResourceService.getOneLoginBgImgByRandom();
    }

}
