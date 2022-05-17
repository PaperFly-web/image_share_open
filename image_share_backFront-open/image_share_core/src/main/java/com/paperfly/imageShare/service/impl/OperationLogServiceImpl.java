package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.OperationLogDao;
import com.paperfly.imageShare.entity.OperationLogEntity;
import com.paperfly.imageShare.service.OperationLogService;
import org.springframework.transaction.annotation.Transactional;


@Service("operationLogService")
@Transactional
public class OperationLogServiceImpl extends ServiceImpl<OperationLogDao, OperationLogEntity> implements OperationLogService {

    @Autowired
    OperationLogDao operationLogDao;

    @Override
    public R getOperationLogs(PageSearchDTO<OperationLogEntity> searchDTO) {
        final QueryWrapper<OperationLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time",searchDTO.getStartTime());
        queryWrapper.le("create_time",searchDTO.getEndTime());
        //添加条件
        if (!EmptyUtil.empty(searchDTO.getCondition())) {
            Map<String, Object> condition = searchDTO.getCondition();
            //判断条件中是否有  操作类型（新增，删除，查询，更新）
            if (!EmptyUtil.empty(condition.get("oper_type"))) {
                queryWrapper.eq("oper_type", condition.get("oper_type"));
            }
            //判断条件中是否有关键字,安装用户ID或者邮箱搜索
            if (!EmptyUtil.empty(condition.get("keyword"))) {
                queryWrapper.eq("user_id", condition.get("keyword"))
                        .or().eq("user_email", condition.get("keyword"));
            }
            //请求方法类型（PUT,GET,DELETE,POST）
            if (!EmptyUtil.empty(condition.get("req_method_type"))) {
                queryWrapper.eq("req_method_type", condition.get("req_method_type"));
            }
            //操作类型（新增，删除，查询，更新）
            if (!EmptyUtil.empty(condition.get("oper_type"))) {
                queryWrapper.eq("oper_type", condition.get("oper_type"));
            }
        }
        final Page<OperationLogEntity> operationLogEntityPage = operationLogDao.selectPage(searchDTO.getPage(), queryWrapper);
        return R.ok("查询成功").put("data",operationLogEntityPage);
    }

    @Override
    public R deleteOperationLogs(List<String> ids) {
        if(EmptyUtil.empty(ids)){
            return R.userError("ids不能为空");
        }
        final int deleteCount = operationLogDao.deleteBatchIds(ids);
        if(deleteCount>0){
            return R.ok("删除成功");
        }else {
            return R.userError("删除失败，可能ID不存在");
        }
    }
}