package com.paperfly.imageShare.aspect;

import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.entity.FocusUserEntity;
import com.paperfly.imageShare.entity.PostCommentEntity;
import com.paperfly.imageShare.entity.RecommendedEntity;
import com.paperfly.imageShare.entity.ThumbEntity;
import com.paperfly.imageShare.service.NotifyService;
import com.paperfly.imageShare.service.RecommendedService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息通知的AOP切面
 */
@Aspect
@Slf4j
@Component
public class NotifyAspect {

    @Autowired
    NotifyService notifyService;

    @AfterReturning(value = "execution(* com.paperfly.imageShare.controller.user.ThumbController.add(com.paperfly.imageShare.entity.ThumbEntity))"
            , returning = "keys")
    public void thumbAddAspect(JoinPoint joinPoint, Object keys) {
        Object[] args = joinPoint.getArgs();
        ThumbEntity thumb = (ThumbEntity) args[0];
        R r = (R) keys;
        if ((int) r.getCode() == 0) {
            //判断是不是自己对自己点赞
            notifyService.addThumbAndNotify(thumb);
        }
    }

    @AfterReturning(value = "execution(public * com.paperfly.imageShare.controller.user.PostCommentController.add(..))"
            , returning = "keys")
    public void postCommentAddAspect(JoinPoint joinPoint, Object keys) {
        Object[] args = joinPoint.getArgs();
        PostCommentEntity postComment = (PostCommentEntity) args[0];
        R r = (R) keys;
        if ((int) r.getCode() == 0) {
            notifyService.addPostCommentAndNotify(postComment);
        }
    }

    @AfterReturning(value = "execution(public * com.paperfly.imageShare.controller.user.FocusUserController.focus(..))"
            , returning = "keys")
    public void focusAddAspect(JoinPoint joinPoint, Object keys) {
        R r = (R) keys;
        if ((int) r.getCode() == 0) {
            FocusUserEntity focusUser = (FocusUserEntity) r.getData();
            notifyService.addFocusAndNotify(focusUser);
        }
    }

}
