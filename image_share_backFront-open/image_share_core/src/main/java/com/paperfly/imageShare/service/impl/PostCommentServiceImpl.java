package com.paperfly.imageShare.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.ListUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.dto.PostCommentDTO;
import com.paperfly.imageShare.dto.SensitiveDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.*;
import com.paperfly.imageShare.vo.PostCommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.PostCommentDao;
import com.paperfly.imageShare.entity.PostCommentEntity;
import org.springframework.transaction.annotation.Transactional;


@Service("postCommentService")
@Transactional
public class PostCommentServiceImpl extends ServiceImpl<PostCommentDao, PostCommentEntity> implements PostCommentService {

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    PostService postService;

    @Autowired
    PostCommentDao postCommentDao;

    @Autowired
    UserService userService;

    @Autowired
    NotifyService notifyService;

    @Override
    public R add(PostCommentEntity postComment) {
        //1.检查评论内容，不超过500字，且不能为空
        if (EmptyUtil.empty(postComment.getOriginalContent()) || postComment.getOriginalContent().length() > 500) {
            return R.userError("评论不能为空，且字数必须在(0,500]之间");
        }
        //2.检查评论帖子的ID是否为空
        if (EmptyUtil.empty(postComment.getPostId())) {
            return R.userError("帖子ID不能为空");
        }
        //3.处理评论内容，检查敏感词.设置state字段是否正常
        final SensitiveDTO sensitiveDTO = sensitiveService.analysisText(postComment.getOriginalContent());
        if (sensitiveDTO.isIll()) {
            postComment.setState(1);
        } else {
            postComment.setState(0);
        }
        /*final DefaultIdentifierGenerator generator = new DefaultIdentifierGenerator();
        //设置ID
        postComment.setId(generator.nextId(postComment) + "");*/
        postComment.setHandleContent(sensitiveDTO.getHandleContent());
        //4.设置create_time,update_time,user_id,snake_name,thumb_count
        postComment.setCreateTime(new Date());
        postComment.setUpdateTime(new Date());
        postComment.setUserId(UserSecurityUtil.getCurrUserId());
        postComment.setSnakeName(UserSecurityUtil.getCurrUserSnakeName());
        if(EmptyUtil.empty(postComment.getFatherCommentId())){
            postComment.setFatherCommentId(null);
        }
        postComment.setThumbCount(0);
        final Integer addCount = postCommentDao.add(postComment);
        //更新帖子的评论数量
        if (addCount > 0) {
            final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postComment.getPostId());
            updateWrapper.setSql("comment_count=comment_count+1");
            postService.update(updateWrapper);
            //查询出刚刚添加的帖子
            final QueryWrapper<PostCommentEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",postComment.getUserId());
            queryWrapper.eq("original_content",postComment.getOriginalContent());
            String createTime = DateUtil.format(postComment.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN);
            queryWrapper.ge("create_time",createTime);
            queryWrapper.eq("snake_name",postComment.getSnakeName());
            PostCommentEntity newPostComment = postCommentDao.selectOne(queryWrapper);
            return R.ok("添加评论成功").put("data",newPostComment);
        } else {
            return R.error("添加评论失败,可能帖子已关闭评论");
        }

    }

    @Override
    public R delete(PostCommentEntity postComment) {
        //1.检查ID是否为空
        if (EmptyUtil.empty(postComment.getId())) {
            return R.userError("评论ID不能为空");
        }
        if(EmptyUtil.empty(postComment.getPostId())){
            return R.userError("帖子ID不能为空");
        }
        //2.判断用户是否拥有当前删除评论的权限
        Boolean hasDeletePerm = (Boolean) currUserHasDeletePermission(postComment.getId()).get("data");
        if(!hasDeletePerm){
            return R.userError("您没有权限删除当前评论");
        }
        //3.删除评论
        Integer deleteCount = postCommentDao.delete(postComment);
        if (deleteCount > 0) {
            //设置帖子的评论数据
            final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postComment.getPostId());
            updateWrapper.setSql("comment_count=comment_count-" + deleteCount);
            postService.update(updateWrapper);
            return R.ok("删除评论成功").put("data", deleteCount);
        } else {
            return R.error("不知道为何，删除评论失败");
        }
    }

    @Override
    public R getPostCommentByPostId(PostCommentDTO postCommentDTO) {
        //步骤
        //1.查询父评论：
        //      特征：father_comment_id为空，帖子id不能为空（但因为是查询，可以不用检查）
        //2.给每个父评论，查询出3个子评论
        //      特征：子评论根据thumb_count降序
        //获取根评论
        Page<PostCommentEntity> rootPostCommentPageRes = (Page<PostCommentEntity>)
                this.getRootPostCommentByPostId(postCommentDTO).get("data");
        //最终返回结果页
        final Page<PostCommentVO> finallyResultPage = new Page<>();
        
        BeanUtils.copyProperties(rootPostCommentPageRes,finallyResultPage);
        final ArrayList<PostCommentVO> postCommentVOList = new ArrayList<>();
        for (PostCommentEntity rootPostComment : rootPostCommentPageRes.getRecords()) {
            PostCommentVO postCommentVO = new PostCommentVO();
            BeanUtils.copyProperties(rootPostComment,postCommentVO);
            //2.构建子评论查询条件
            //  需要父ID，根据thumb_count降序排序，只查询3个
            final PostCommentDTO childrenPostCommentSearch = new PostCommentDTO();
            List<OrderItem> orderItems = new ArrayList<>();
            OrderItem orderItem1 = new OrderItem();
            OrderItem orderItem2 = new OrderItem();
            orderItem1.setColumn("thumb_count");
            orderItem1.setAsc(false);
                orderItem2.setColumn("update_time");
            orderItem2.setAsc(false);
            orderItems.add(orderItem1);
            orderItems.add(orderItem2);
            childrenPostCommentSearch.setOrders(orderItems);
            childrenPostCommentSearch.setFatherCommentId(rootPostComment.getId());
            childrenPostCommentSearch.setCurrent(1);
            childrenPostCommentSearch.setSize(3);


            Page<PostCommentEntity> childrenPostCommentPage = (Page<PostCommentEntity>)
                    this.getChildrenPostCommentByPostId(childrenPostCommentSearch).get("data");
            postCommentVO.setChildrenPostComments(childrenPostCommentPage);
            postCommentVO.setChildrenCount((int) childrenPostCommentPage.getTotal());
            postCommentVOList.add(postCommentVO);
        }
        finallyResultPage.setRecords(postCommentVOList);
        return R.ok("查询成功").put("data",finallyResultPage);
    }

    @Override
    public R getRootPostCommentByPostId(PostCommentDTO postCommentDTO) {
        final QueryWrapper<PostCommentEntity> queryWrapper = new QueryWrapper<>();
        //通过帖子ID查询帖子的第一级评论
        //第一级评论：父ID为空
        queryWrapper.eq("post_id", postCommentDTO.getPostId());
        queryWrapper.isNull("father_comment_id");
        queryWrapper.select("id", "handle_content", "snake_name", "user_id",
                "post_id", "state", "create_time", "update_time", "thumb_count");
        final Page<PostCommentEntity> rootPostCommentPageRes = postCommentDao.selectPage(postCommentDTO, queryWrapper);
        return R.ok("查询根评论成功").put("data", rootPostCommentPageRes);
    }

    @Override
    public R getChildrenPostCommentByPostId(PostCommentDTO postCommentDTO) {
        final QueryWrapper<PostCommentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("father_comment_id", postCommentDTO.getFatherCommentId());
        queryWrapper.select("id", "father_comment_id", "replay_user_snake_name",
                "handle_content", "snake_name", "user_id", "post_id", "state", "create_time", "update_time", "thumb_count");
        final Page<PostCommentEntity> childrenCommentPageRes = postCommentDao.selectPage(postCommentDTO, queryWrapper);
        for (PostCommentEntity record : childrenCommentPageRes.getRecords()) {
            final String userId = record.getUserId();
            userService.getHeadImg(userId);

        }
        return R.ok("查询子评论成功").put("data", childrenCommentPageRes);
    }

    @Override
    public R currUserHasDeletePermission(String postCommentId) {
        //1。判断ID是否为空
        if(EmptyUtil.empty(postCommentId)){
            return R.ok().put("data",false);
        }
        //判断当前用户是否为管理员,如果是管理员那就有权限删除
        if(ListUtil.contains(UserSecurityUtil.getCurrUserRole(),1,2)){
            return R.ok().put("data",true);
        }
        //2.判断评论ID是否为当前用户拥有的
        final QueryWrapper<PostCommentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",postCommentId);
        queryWrapper.select("id","post_id","father_comment_id","user_id");
        final PostCommentEntity postCommentEntity = postCommentDao.selectOne(queryWrapper);
        if(EmptyUtil.empty(postCommentEntity)){
            return R.ok().put("data",false);
        }else if(UserSecurityUtil.getCurrUserId().equals(postCommentEntity.getUserId())){
            return R.ok().put("data",true);
        }else {
            //3.判断评论的帖子是否为当前用户拥有的
            final QueryWrapper<PostEntity> postQueryWrapper = new QueryWrapper<>();
            postQueryWrapper.eq("id",postCommentEntity.getPostId());
            postQueryWrapper.select("id","user_id");
            final PostEntity postEntity = postService.getOne(postQueryWrapper);
            if(EmptyUtil.empty(postEntity)){
                return R.ok().put("data",false);
            }
            if(UserSecurityUtil.getCurrUserId().equals(postEntity.getUserId())){
                return R.ok().put("data",true);
            }
        }

        return R.ok().put("data",false);
    }

    @Override
    public R getPostCommentById(String id) {
        final PostCommentEntity postComment = this.getById(id);
        return R.ok("查询成功").put("data",postComment);
    }


}