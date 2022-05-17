package com.paperfly.imageShare.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.service.FileService;
import com.paperfly.imageShare.service.StaticResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StaticResourceServiceImpl implements StaticResourceService {
    @Autowired
    FileService fileService;

    @Value("${imageShare.loginBgFolder}")
    String loginBgFolder;

    @Override
    public R getLoginBgImg() {
        List<String> loginBgImgUrls = fileService.getFolderFileUrl(loginBgFolder, false);
        return R.ok("获取成功").put("data",loginBgImgUrls);
    }

    @Override
    public R getLoginBgImg(String fileName) {
        String fileUrl = fileService.getFileUrl(loginBgFolder + fileName, false);
        return R.ok("获取成功").put("data",fileUrl);
    }

    @Override
    public R getOneLoginBgImgByRandom() {
        List<String> loginBgImgUrls = fileService.getFolderFileUrl(loginBgFolder, false);
        final int random = RandomUtil.randomInt(loginBgImgUrls.size());
        final String fileUrl = loginBgImgUrls.get(random);
        return R.ok("获取成功").put("data",fileUrl);
    }
}
