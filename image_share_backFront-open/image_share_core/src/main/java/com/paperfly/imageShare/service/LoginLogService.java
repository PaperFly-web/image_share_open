package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.LoginLogEntity;
import com.paperfly.imageShare.entity.OperationLogEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-01-29 19:37:25
 */
public interface LoginLogService extends IService<LoginLogEntity> {

    /**
     * 管理员获取登录日志
     * @param searchDTO
     * @return
     */
    R getLoginLogs(PageSearchDTO<LoginLogEntity> searchDTO);

    /**
     * 批量删除登录日志
     * @param ids
     * @return
     */
    R deleteLoginLogs(List<String> ids);
}

