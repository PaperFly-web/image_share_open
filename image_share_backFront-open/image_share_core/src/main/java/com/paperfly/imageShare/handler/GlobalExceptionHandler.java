package com.paperfly.imageShare.handler;

import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

/**
*@desc:全局异常处理器
*@param:* @param null:
*@return:* @return: null
*@author:paperfly
*@time:2020/8/21 22:34
*/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R  doException(Exception e){
        log.error("系统异常"+e.getMessage());
        e.printStackTrace();
        return R.error().put("msg","系统出现了异常").put("reason",e.getMessage());
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public R  doRedisConnectionFailureException(Exception e){
        log.error("redis连接异常"+e.getMessage());
        return R.error().put("msg","系统出现了异常").put("reason",e.getMessage());
    }


    @ExceptionHandler(AccessDeniedException.class)
    public R doAccessDeniedException(AccessDeniedException e){
        log.error("权限不足"+e.getMessage());
        return R.error().put("msg","你的权限不足").put("code", CodeConstant.UN_PERMISSIONS);
    }

}