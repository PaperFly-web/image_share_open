package com.paperfly.imageShare.controller.open;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-26 14:34:50
 */
@RestController
@RequestMapping("open/user")
public class OpenUserController {
    @Autowired
    private UserService userService;

    @GetMapping("/checkEmailIsExist/{email}")
    @OperLog(operModule = "用户",operDesc = "检查邮箱是否已被注册",operType = OperTypeConst.SELECT)
    public R checkEmailIsExist(@PathVariable("email")String email){
        if (userService.checkEmailIsExist(email)) {
            return R.userError("此邮箱【" + email + "】已被注册");
        }else {
            return R.ok("当前邮箱暂未被注册");
        }
    }

    @GetMapping("/checkSnakeNameIsExist/{snakeName}")
    @OperLog(operModule = "IP地址",operDesc = "检查snakename是否已被注册",operType = OperTypeConst.SELECT)
    public R checkSnakeNameIsExist(@PathVariable("snakeName")String snakeName){
        if (userService.checkSnakeNameIsExist(snakeName)) {
            return R.userError("此昵称【" + snakeName + "】已被注册");
        }else {
            return R.ok("当前昵称暂未被注册");
        }
    }

    @GetMapping("/checkTokenIsValid/{token}")
    @OperLog(operModule = "IP地址",operDesc = "检查token是否有效",operType = OperTypeConst.SELECT)
    public R checkTokenIsValid(@PathVariable("token")String token){
        final Boolean isValid = userService.checkTokenIsValid(token);
        if(isValid){
            return R.ok("当前token有效").put("data",token);
        }else {
            return R.error("当前token无效").put("code", CodeConstant.ERROR).put("data",token);
        }
    }


}
