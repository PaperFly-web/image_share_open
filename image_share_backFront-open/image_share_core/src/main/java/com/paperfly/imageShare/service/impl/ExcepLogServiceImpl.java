package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dao.OperationLogDao;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.OperationLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.ExcepLogDao;
import com.paperfly.imageShare.entity.ExcepLogEntity;
import com.paperfly.imageShare.service.ExcepLogService;
import org.springframework.transaction.annotation.Transactional;


@Service("excepLogService")
@Transactional
public class ExcepLogServiceImpl extends ServiceImpl<ExcepLogDao, ExcepLogEntity> implements ExcepLogService {

    @Autowired
    ExcepLogDao excepLogDao;

    @Override
    public R getExcepLogs(PageSearchDTO<ExcepLogEntity> searchDTO) {
        final QueryWrapper<ExcepLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time",searchDTO.getStartTime());
        queryWrapper.le("create_time",searchDTO.getEndTime());
        //展示不添加条件
        if (!EmptyUtil.empty(searchDTO.getCondition())) {
            Map<String, Object> condition = searchDTO.getCondition();
            //判断条件中是否有  请求方法类型
            if (!EmptyUtil.empty(condition.get("req_method_type"))) {
                queryWrapper.eq("req_method_type", condition.get("req_method_type"));
            }
        }
        final Page<ExcepLogEntity> excepLogEntityPage = excepLogDao.selectPage(searchDTO.getPage(), queryWrapper);
        return R.ok("查询成功").put("data",excepLogEntityPage);
    }

    @Override
    public R deleteExcepLogs(List<String> ids) {
        if(EmptyUtil.empty(ids)){
            return R.userError("ids不能为空");
        }
        final int deleteCount = excepLogDao.deleteBatchIds(ids);
        if(deleteCount>0){
            return R.ok("删除成功");
        }else {
            return R.userError("删除失败，可能ID不存在");
        }
    }
}