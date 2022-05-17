package com.paperfly.imageShare.service.impl;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;
import com.paperfly.imageShare.common.utils.CaptchaUtil;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service("imageCaptchaService")
@Slf4j
public class ImageCaptchaServiceImpl implements CaptchaService<byte[]> {
    @Autowired
    RedisTemplate redisTemplate;

    private String redisPrefixKey = "IMAGE_Captcha_";

    @Override
    public byte[] getCaptcha(String macCode) {
        Object[] captcha = CaptchaUtil.createImage();
        String randomStr = (String) captcha[0];
        log.info("randomStr  result: " + randomStr);
        //先删除缓存中的验证码
        redisTemplate.delete(redisPrefixKey + macCode);
        redisTemplate.opsForValue().set(redisPrefixKey + macCode, randomStr);
        //设置过期时间为10分钟
        redisTemplate.expire(redisPrefixKey + macCode, 10, TimeUnit.MINUTES);
        return (byte[]) captcha[1];
    }

    @Override
    public boolean verifyCaptcha(String macCode, String userCaptcha) {
        if(EmptyUtil.empty(macCode)||EmptyUtil.empty(userCaptcha)){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get(redisPrefixKey + macCode);
        if (EmptyUtil.empty(redisCaptcha)) {
            return false;
        } else {
            return redisCaptcha.equalsIgnoreCase(userCaptcha);
        }
    }

    @Override
    public Boolean removeCaptcha(String uniqueIdentification) {
        if(EmptyUtil.empty(uniqueIdentification)){
            return false;
        }
        return redisTemplate.delete(redisPrefixKey+uniqueIdentification);
    }
}
