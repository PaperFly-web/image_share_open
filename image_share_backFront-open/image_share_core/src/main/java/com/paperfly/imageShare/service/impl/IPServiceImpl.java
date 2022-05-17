package com.paperfly.imageShare.service.impl;

import com.paperfly.imageShare.common.utils.IpUtil;
import com.paperfly.imageShare.dto.IPDTO;
import com.paperfly.imageShare.service.IPService;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

@Service("iPService")
@Slf4j
public class IPServiceImpl implements IPService {

    @Override
    public String getIp(HttpServletRequest request) {
        return IpUtil.getIpAddr(request);
    }

    @Override
    public IPDTO analysisIP(String ip) {
        //默认地址，如果解析不出来
        String region = "中国|0|北京|北京市|0";
        IPDTO ipdto = new IPDTO();
        try {
            DbConfig config = new DbConfig();
            String dbfile = this.getClass().getClassLoader().getResource("data/ip2region.db").getPath();
            DbSearcher searcher = new DbSearcher(config, dbfile);
            DataBlock block = searcher.btreeSearch(ip);
            region = block.getRegion();
        }catch (Exception e){
            log.error("解析IP信息发生错误："+e.getMessage());
        }

        String[] addressInfo = region.split("\\|");
        StringBuffer place = new StringBuffer();
        place.append(addressInfo[0]).append("|")
                .append(addressInfo[1]).append("|")
                .append(addressInfo[2]).append("|")
                .append(addressInfo[3]);
        ipdto.setPlace(place.toString());
        ipdto.setCountry(addressInfo[0]);
        ipdto.setArea(addressInfo[1]);
        ipdto.setRegion(addressInfo[2]);
        ipdto.setCity(addressInfo[3]);
        ipdto.setIsp(addressInfo[4]);
        ipdto.setIp(ip);
        return ipdto;
    }

    @Override
    public IPDTO analysisIP(HttpServletRequest request) {
        String ip = this.getIp(request);
        return this.analysisIP(ip);
    }
}
