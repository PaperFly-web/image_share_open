package com.paperfly.imageShare.service;

public interface CaptchaService<T> {

    /**
     * 获取验证码
     *
     * @param uniqueIdentification :唯一标识，用于存储在redis中的key
     * @return 请求的验证码
     */
    T getCaptcha(String uniqueIdentification);


    /**
     * 校验验证码
     *
     * @param uniqueIdentification 请求验证码时候的唯一标识
     * @param captchaCode 验证码的值
     * @return 验证码是否正确
     */
    boolean verifyCaptcha(String uniqueIdentification, String captchaCode);

    /**
     * 移除验证码
     * @param uniqueIdentification 请求验证码时候的唯一标识
     */
    Boolean removeCaptcha(String uniqueIdentification);

}
