package com.paperfly.imageShare.vo;

import com.paperfly.imageShare.common.entity.BaseEntity;
import lombok.Data;

@Data
public class DialogVO extends BaseEntity {

    private String snakeName;

    private String username;

    private String headImage;

    /**
     * 与这个用户的对话还有多少未读
     */
    private Integer noReadCount;
}
