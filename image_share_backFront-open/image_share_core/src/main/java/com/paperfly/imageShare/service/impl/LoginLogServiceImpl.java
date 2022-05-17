package com.paperfly.imageShare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.OperationLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.LoginLogDao;
import com.paperfly.imageShare.entity.LoginLogEntity;
import com.paperfly.imageShare.service.LoginLogService;
import org.springframework.transaction.annotation.Transactional;


@Service("loginLogService")
@Transactional
public class LoginLogServiceImpl extends ServiceImpl<LoginLogDao, LoginLogEntity> implements LoginLogService {

    @Autowired
    LoginLogDao loginLogDao;

    @Override
    public R getLoginLogs(PageSearchDTO<LoginLogEntity> searchDTO) {
        final QueryWrapper<LoginLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time",searchDTO.getStartTime());
        queryWrapper.le("create_time",searchDTO.getEndTime());
        //添加条件
        if (!EmptyUtil.empty(searchDTO.getCondition())) {
            Map<String, Object> condition = searchDTO.getCondition();
            //判断条件中是否有  登录结果
            if (!EmptyUtil.empty(condition.get("result"))) {
                queryWrapper.eq("result", condition.get("result"));
            }
            //判断条件中是否有关键字,用户ID或者邮箱搜索
            if (!EmptyUtil.empty(condition.get("keyword"))) {
                queryWrapper.eq("user_id", condition.get("keyword"))
                        .or().eq("user_email", condition.get("keyword"));
            }
        }
        final Page<LoginLogEntity> loginLogEntityPage = loginLogDao.selectPage(searchDTO.getPage(), queryWrapper);
        return R.ok("查询成功").put("data",loginLogEntityPage);
    }

    @Override
    public R deleteLoginLogs(List<String> ids) {
        if(EmptyUtil.empty(ids)){
            return R.userError("ids不能为空");
        }
        final int deleteCount = loginLogDao.deleteBatchIds(ids);
        if(deleteCount>0){
            return R.ok("删除成功");
        }else {
            return R.userError("删除失败，可能ID不存在");
        }
    }
}