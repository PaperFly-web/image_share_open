package com.paperfly.imageShare.service;

import com.paperfly.imageShare.dto.IPDTO;
import org.lionsoul.ip2region.DbMakerConfigException;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

public interface IPService {

    /**
     * 根据HttpServletRequest 获取IP地址
     * @param request
     * @return
     */
    String getIp(HttpServletRequest request);

    /**
     * 根据IP分析出  国家|区域|省份|城市|服务商
     * @param ip
     * @return
     */
    IPDTO analysisIP(String ip);

    /**
     * 根据HttpServletRequest，解析出IP，然后在分析出 国家|区域|省份|城市|服务商
     * @param request
     * @return
     */
    IPDTO analysisIP(HttpServletRequest request);
}
