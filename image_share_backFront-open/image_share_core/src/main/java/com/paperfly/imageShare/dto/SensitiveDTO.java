package com.paperfly.imageShare.dto;

import lombok.Data;

import java.util.List;

@Data
public class SensitiveDTO {
    /**
     * 评论未处理时内容
     */
    private String originalContent;
    /**
     * 评论处理后的内容
     */
    private String handleContent;

    /**
     * 违规类型
     */
    private List<String> type;

    /**
     * 是否违规
     */
    private boolean isIll;
}
