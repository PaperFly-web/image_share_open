package com.paperfly.imageShare.controller.open;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.dto.IPDTO;
import com.paperfly.imageShare.service.IPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("open/ip")
public class IPController {

    @Autowired
    IPService ipService;

    @GetMapping("/currentIp")
    @OperLog(operModule = "IP地址",operDesc = "获取当前IP",operType = OperTypeConst.SELECT)
    public String getCurrentIp(HttpServletRequest request) {
        return ipService.getIp(request);
    }

    @GetMapping("/currentLocation")
    @OperLog(operModule = "IP地址",operDesc = "获取当前位置信息",operType = OperTypeConst.SELECT)
    public IPDTO getCurrentLocation(HttpServletRequest request) {
        return ipService.analysisIP(request);
    }

    @GetMapping("/{ip}")
    @OperLog(operModule = "IP地址",operDesc = "获取指定IP位置信息",operType = OperTypeConst.SELECT)
    public IPDTO getLocationByIp(@PathVariable String ip) {
        return ipService.analysisIP(ip);
    }

}
