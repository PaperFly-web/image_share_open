package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
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
public interface OperationLogService extends IService<OperationLogEntity> {

    /**
     * 管理员获取操作日志
     * @param searchDTO
     * @return
     */
    R getOperationLogs(PageSearchDTO<OperationLogEntity> searchDTO);

    /**
     * 批量删除操作日志
     * @param ids
     * @return
     */
    R deleteOperationLogs(List<String> ids);
}

