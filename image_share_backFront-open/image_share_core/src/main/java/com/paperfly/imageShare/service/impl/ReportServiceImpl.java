package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.constant.NotifyConst;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.ListUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.*;
import com.paperfly.imageShare.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.ReportDao;
import org.springframework.transaction.annotation.Transactional;


@Service("reportService")
@Transactional
public class ReportServiceImpl extends ServiceImpl<ReportDao, ReportEntity> implements ReportService {

    @Autowired
    ReportDao reportDao;

    @Autowired
    UserService userService;

    @Autowired
    PostCommentService postCommentService;

    @Autowired
    PostService postService;

    @Autowired
    NotifyService notifyService;


    @Override
    public R add(ReportEntity report) {
        if (EmptyUtil.empty(report)) {
            return R.error("实体类为空");
        }
        //1.检查举报分类ID是否为空
        if (EmptyUtil.empty(report.getReportTypeId())) {
            return R.userError("举报分类ID为空");
        }
        //2.检查被举报ID（用户，帖子，评论）是否为空
        if (EmptyUtil.empty(report.getReportId())) {
            return R.userError("被举报ID为空");
        }
        //3.检查举报内容是否为空
        if (EmptyUtil.empty(report.getReportContent())) {
            return R.userError("举报内容为空");
        }
        //4.检查举报类别是否正确
        if (EmptyUtil.empty(report.getReportType()) ||
                (report.getReportType() != 0 &&
                        report.getReportType() != 1 &&
                        report.getReportType() != 2)){
            return R.userError("举报类别错误");
        }
            //5.设置举报用户，时间，举报状态，管理员ID，审核时间
        report.setUserId(UserSecurityUtil.getCurrUserId());
        report.setState(0);
        report.setCreateTime(new Date());
        report.setUpdateTime(new Date());
        report.setAuditTime(null);
        report.setManagerId(null);
        report.setId(null);
        final int insertCout = reportDao.insert(report);
        if(insertCout>0){
            return R.ok("感谢您对ImageShare平台维护的支持");
        }else {
            return R.userError("不知道为何添加举报失败");
        }
    }


    @Override
    public R detailManyReport(Integer isIll,List<String> ids) {
        //检查举报IDS
        if(EmptyUtil.empty(ids)){
            return R.userError("集合不能为空");
        }
        if(EmptyUtil.empty(isIll) || !ListUtil.contains(isIll,0,1)){
            return R.userError("是否违规数据错误");
        }
        final UpdateWrapper<ReportEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",ids);
        updateWrapper.set("state",1);
        updateWrapper.set("is_ill",isIll);
        updateWrapper.set("update_time",new Date());
        updateWrapper.set("audit_time",new Date());
        updateWrapper.set("manager_id",UserSecurityUtil.getCurrUserId());
        final int updateCount = reportDao.update(null, updateWrapper);
        if(updateCount>0){
            return R.ok("处理举报成功");
        }else {
            return R.error("处理举报失败");
        }
    }


    @Override
    public R getReports(PageSearchDTO<ReportEntity> searchDTO) {
        final QueryWrapper<ReportEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time",searchDTO.getStartTime());
        queryWrapper.le("create_time",searchDTO.getEndTime());

        //添加条件
        if(!EmptyUtil.empty(searchDTO.getCondition())){
            Map<String, Object> condition = searchDTO.getCondition();
            //判断条件中是否有状态
            if(!EmptyUtil.empty(condition.get("state"))){
                queryWrapper.eq("state",condition.get("state"));
            }
            //判断条件中是否有关键字
            if(!EmptyUtil.empty(condition.get("keyword"))){
                final R userRes = userService.findUserBySnakeNameOrEmail((String) condition.get("keyword"));
                List<UserEntity> users = new ArrayList<>();
                if(userRes.getCode() == 0){
                    users = ((Page<UserEntity>) userRes.getData()).getRecords();
                }else {
                    return R.ok("查询成功").put("data",new Page<PostDTO>());
                }
                if(users.size() == 0){
                    return R.ok("查询成功").put("data",new Page<PostDTO>());
                }
                final List<String> userIds = users.stream().map(x -> {
                    return x.getId();
                }).collect(Collectors.toList());
                queryWrapper.in("user_id",userIds);
            }
            //判断条件是否有举报类别
            if(!EmptyUtil.empty(condition.get("report_type"))){
                queryWrapper.eq("report_type",condition.get("report_type"));
            }
            //判断条件是否有违规
            if(!EmptyUtil.empty(condition.get("is_ill"))){
                queryWrapper.eq("is_ill",condition.get("is_ill"));
            }
        }


        Page<ReportEntity> reportEntityPage = reportDao.selectPage(searchDTO.getPage(), queryWrapper);

        return R.ok("查询成功").put("data",reportEntityPage);
    }

    @Override
    public R addAndNotifyUserReportState(String isIll,ReportEntity report) {
        String content ="";//系统消息的具体内容
        if(EmptyUtil.empty(report)){
            return R.userError("实体类为空");
        }
        //查询具体举报内容
        final String reportId = report.getReportId();
        final Integer reportType = report.getReportType();
        if(EmptyUtil.empty(reportId)){
            return R.userError("举报目标ID为空");
        }
        if (!ListUtil.contains(reportType,0,1,2)){
            return R.userError("举报类型错误");
        }
        //举报用户
        if(reportType == 0){
            final UserEntity user = userService.getById(reportId);
            if(EmptyUtil.empty(user)){
                content = "经查实您举报的用户【已删除】";
            }else {
                content = "经查实您举报的用户【昵称："+user.getSnakeName()+"，ID："+user.getId()+"】";
            }
            //举报评论
        }else if(reportType == 1){
            final PostCommentEntity postCommentEntity = postCommentService.getById(reportId);
            if(EmptyUtil.empty(postCommentEntity)){
                content = "经查实您举报的评论【已删除】";
            }else {
                content = "经查实您举报的评论【内容："+postCommentEntity.getHandleContent()+"，ID："+postCommentEntity.getId()+"】";
            }
            //举报帖子
        }else if (reportType == 2){
            final PostEntity postEntity = postService.getById(reportId);
            if(EmptyUtil.empty(postEntity)){
                content = "经查实您举报的帖子【已删除】";
            }else {
                content = "经查实您举报的帖子【内容："+postEntity.getHandleContent()+"，ID："+postEntity.getId()+"】";
            }
        }
        if ("0".equals(isIll)){
            content+="确实违规";
        }else {
            content+="未发现违规";
        }

        final NotifyEntity notifyEntity = new NotifyEntity();
        notifyEntity.setContent(content);
        notifyEntity.setType(NotifyConst.Type.REMIND.getType());
        notifyEntity.setTargetId(report.getReportId());
        //判断被举报的类型  0：举报用户1：举报评论2：举报帖子
        if(report.getReportType() == 0){
            notifyEntity.setTargetType(NotifyConst.TargetType.ONE_USER.getTargetType());
        }else if(report.getReportType() == 1){
            notifyEntity.setTargetType(NotifyConst.TargetType.COMMENT.getTargetType());
        }else if(report.getReportType() == 2){
            notifyEntity.setTargetType(NotifyConst.TargetType.POST.getTargetType());
        }
        notifyEntity.setAction(NotifyConst.Action.SYSTEM_MESSAGE.getAction());
        //当前正在审核的管理员ID
        notifyEntity.setSenderId(UserSecurityUtil.getCurrUserId());
        notifyEntity.setSenderType(NotifyConst.SenderType.ADMIN.getSenderType());
        notifyEntity.setIsRead(0);
        notifyEntity.setUserId(report.getUserId());
        notifyEntity.setCreateTime(new Date());
        notifyEntity.setUpdateTime(new Date());
        return notifyService.addAndNotify(notifyEntity);
    }

}