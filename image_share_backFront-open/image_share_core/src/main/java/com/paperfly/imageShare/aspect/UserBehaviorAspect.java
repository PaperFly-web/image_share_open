package com.paperfly.imageShare.aspect;

import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.entity.*;
import com.paperfly.imageShare.service.RecommendedService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class UserBehaviorAspect {

    @Autowired
    RecommendedService recommendedService;

    /**
     * 点赞的切面
     * @param joinPoint
     * @param keys
     */
    /*@AfterReturning(value = "execution(* com.paperfly.imageShare.controller.user.ThumbController.add(com.paperfly.imageShare.entity.ThumbEntity))"
            , returning = "keys")*/
    public void thumbAddAspect(JoinPoint joinPoint, Object keys){
        Object[] args = joinPoint.getArgs();
        ThumbEntity thumb = (ThumbEntity)args[0];
        R r = (R)keys;
        if((int)r.getCode()==0){
            //判断是不是对帖子的点赞
            if(thumb.getThumbType()==0){
                final RecommendedEntity recommendedEntity = new RecommendedEntity();
                recommendedEntity.setUserId(thumb.getUserId());
                recommendedEntity.setPostId(thumb.getToId());
                recommendedEntity.setScore(2f);
                recommendedService.add(recommendedEntity);
            }
        }
    }

    /**
     * 评论的切面
     * @param joinPoint
     * @param keys
     */
    /*@AfterReturning(value = "execution(public * com.paperfly.imageShare.controller.user.PostCommentController.add(..))"
            , returning = "keys")*/
    public void postCommentAddAspect(JoinPoint joinPoint, Object keys){
        Object[] args = joinPoint.getArgs();
        PostCommentEntity postComment = (PostCommentEntity)args[0];
        R r = (R)keys;
        if((int)r.getCode()==0){
            final RecommendedEntity recommendedEntity = new RecommendedEntity();
            recommendedEntity.setUserId(postComment.getUserId());
            recommendedEntity.setPostId(postComment.getPostId());
            recommendedEntity.setScore(4f);
            recommendedService.add(recommendedEntity);
        }
    }

    /**
     * 浏览详情的切面
     * @param joinPoint
     * @param keys
     */
    /*@AfterReturning(value = "execution(public * com.paperfly.imageShare.controller.user.ViewDetailsController.add(..))"
            , returning = "keys")*/
    public void viewsAddAspect(JoinPoint joinPoint, Object keys){
        Object[] args = joinPoint.getArgs();
        ViewDetailsEntity viewDetails = (ViewDetailsEntity)args[0];
        R r = (R)keys;
        if((int)r.getCode()==0){
            final RecommendedEntity recommendedEntity = new RecommendedEntity();
            recommendedEntity.setUserId(viewDetails.getUserId());
            recommendedEntity.setPostId(viewDetails.getPostId());
            recommendedEntity.setScore(1f);
            recommendedService.add(recommendedEntity);
        }
    }

    /**
     * 收藏的切面
     * @param joinPoint
     * @param keys
     */
    /*@AfterReturning(value = "execution(public * com.paperfly.imageShare.service.impl.FavlistServiceImpl.add(..))"
            , returning = "keys")*/
    public void favlistAddAspect(JoinPoint joinPoint, Object keys){
        Object[] args = joinPoint.getArgs();
        FavlistEntity favlist = (FavlistEntity)args[0];
        R r = (R)keys;
        if((int)r.getCode()==0){
            final RecommendedEntity recommendedEntity = new RecommendedEntity();
            recommendedEntity.setUserId(favlist.getUserId());
            recommendedEntity.setPostId(favlist.getPostId());
            recommendedEntity.setScore(5f);
            recommendedService.add(recommendedEntity);
        }
    }
}
