package com.paperfly.imageShare.service;

import com.paperfly.imageShare.dto.SensitiveDTO;

/**
 * 敏感词检测
 */
public interface SensitiveService {
    /**
     * 分析文本，返回分析结果
     * @param text 要分析的文本
     * @return
     */
    SensitiveDTO analysisText(String text);
}
