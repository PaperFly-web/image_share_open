package com.paperfly.imageShare.service.impl;

import com.google.protobuf.Empty;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.baidu.Base64Util;
import com.paperfly.imageShare.common.utils.baidu.FileUtil;
import com.paperfly.imageShare.common.utils.baidu.GsonUtils;
import com.paperfly.imageShare.common.utils.baidu.HttpUtil;
import com.paperfly.imageShare.dto.ImgCensorDTO;
import com.paperfly.imageShare.service.AuthService;
import com.paperfly.imageShare.service.ImgCensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URLEncoder;

@Service("imgCensorService")
@Slf4j
public class ImgCensorServiceImpl implements ImgCensorService {
    @Autowired
    @Qualifier("baiDuAuthService")
    AuthService<String> baiDuAuthService;

    // 请求url
    private final String url = "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined";

    @Override
    public String imgCensor(String fileUrl) {
        String accessToken = baiDuAuthService.getAuth();
        try {
            String imgParam = URLEncoder.encode(fileUrl,"UTF-8");
            String param = "imgUrl=" + imgParam;
            String result = HttpUtil.post(url, accessToken, param);
            log.info("图片检测结果：" + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String imgCensor(InputStream imageIns) {
        String accessToken = baiDuAuthService.getAuth();
        try {
            final byte[] imgData = FileUtil.readInputStreamByBytes(imageIns);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
            String param = "image=" + imgParam;
            String result = HttpUtil.post(url, accessToken, param);
            log.info("图片检测结果：" + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ImgCensorDTO imgCensorResDTO(String fileUrl) {
        final String resJson = this.imgCensor(fileUrl);
        final ImgCensorDTO imgCensorDTO = GsonUtils.fromJson(resJson, ImgCensorDTO.class, true);
        if(EmptyUtil.empty(imgCensorDTO) || !("合规".equals(imgCensorDTO.getConclusion()))){
            imgCensorDTO.setIsIll(true);
        }else {
            imgCensorDTO.setIsIll(false);
        }
        return imgCensorDTO;
    }

    @Override
    public ImgCensorDTO imgCensorResDTO(InputStream fileUrl) {
        final String resJson = this.imgCensor(fileUrl);
        final ImgCensorDTO imgCensorDTO = GsonUtils.fromJson(resJson, ImgCensorDTO.class, true);
        if(EmptyUtil.empty(imgCensorDTO) || !("合规".equals(imgCensorDTO.getConclusion()))){
            imgCensorDTO.setIsIll(true);
        }else {
            imgCensorDTO.setIsIll(false);
        }
        return imgCensorDTO;
    }

}
