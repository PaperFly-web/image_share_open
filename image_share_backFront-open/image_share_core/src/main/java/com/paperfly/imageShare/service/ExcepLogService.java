package com.paperfly.imageShare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.entity.ExcepLogEntity;
import com.paperfly.imageShare.entity.OperationLogEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author paperfly
 * @email 1430978392@qq.com
 * @date 2022-02-01 14:21:07
 */
public interface ExcepLogService extends IService<ExcepLogEntity> {

    /**
     * 管理员获取异常日志
     * @param searchDTO
     * @return
     */
    R getExcepLogs(PageSearchDTO<ExcepLogEntity> searchDTO);

    /**
     * 批量删除异常日志
     * @param ids
     * @return
     */
    R deleteExcepLogs(List<String> ids);
}

