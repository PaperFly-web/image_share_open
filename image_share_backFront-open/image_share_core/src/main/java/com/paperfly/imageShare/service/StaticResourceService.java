package com.paperfly.imageShare.service;

import com.paperfly.imageShare.common.utils.R;

public interface StaticResourceService {
    /**
     * 获取所有登录背景图
     * @return
     */
    R getLoginBgImg();

    /**
     * 获取指定登录背景图
     * @param fileName 指定文件名
     * @return
     */
    R getLoginBgImg(String fileName);

    /**
     * 随机获取一张背景图
     * @return
     */
    R getOneLoginBgImgByRandom();
}
