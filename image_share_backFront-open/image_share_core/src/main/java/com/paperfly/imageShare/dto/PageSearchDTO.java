package com.paperfly.imageShare.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class PageSearchDTO<T> {
    /**
     * 搜索页
     */
    private Page<T> page;

    private Date startTime;

    private Date endTime;

    /**
     * 搜索条件
     */
    private Map<String,Object> condition;

    public void setEndTime(Date endTime) {
        if (EmptyUtil.empty(endTime)){
            //默认过去30天
            this.endTime = new Date(-24L * 60 * 60 * 1000 * 30 * 1);
        }else {
            this.endTime = endTime;
        }
    }

    public void setStartTime(Date startTime) {
        if(EmptyUtil.empty(startTime)){
            this.startTime = new Date();
        }else {
            this.startTime = startTime;
        }
    }

    public void setPage(Page<T> page) {
        if (EmptyUtil.empty(page)){
            this.page = new Page<T>();
        }else {
            this.page = page;
        }
    }
}
