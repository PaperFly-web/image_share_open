package com.paperfly.imageShare.dto;

import lombok.Data;

@Data
public class IPDTO {
    /**
     * 地点的原始数据
     */
    private String place;
    /**
     * 国家
     */
    private String country;
    /**
     * 区域
     */
    private String area;
    /**
     * 省份
     */
    private String region;
    /**
     * 城市
     */
    private String city;

    /**
     * 服务商
     */
    private String isp;

    /**
     * 原始IP
     */
    private String ip;

}
