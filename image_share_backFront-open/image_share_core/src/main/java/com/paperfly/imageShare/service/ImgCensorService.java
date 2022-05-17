package com.paperfly.imageShare.service;

import com.paperfly.imageShare.dto.ImgCensorDTO;

import java.io.InputStream;

/**
 * 图片审核
 */
public interface ImgCensorService {

    String imgCensor(String fileUrl);

    String imgCensor(InputStream imageIns);

    /**
     * 图片审核 返回结果为DTO
     * @param fileUrl 网络图片URL
     * @return
     */
    ImgCensorDTO imgCensorResDTO(String fileUrl);

    /**
     * 图片审核 返回结果为DTO
     * @param imageIns 图片流
     * @return
     */
    ImgCensorDTO imgCensorResDTO(InputStream imageIns);
}
