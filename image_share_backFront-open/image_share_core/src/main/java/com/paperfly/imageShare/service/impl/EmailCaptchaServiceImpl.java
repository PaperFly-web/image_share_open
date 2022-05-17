package com.paperfly.imageShare.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.IpUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.dto.EmailDTO;
import com.paperfly.imageShare.service.CaptchaService;
import com.paperfly.imageShare.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Service("emailCaptchaService")
@Slf4j
public class EmailCaptchaServiceImpl implements CaptchaService<R> {
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private EmailService emailService;

    @Autowired
    RedisTemplate redisTemplate;

    //获取当前的请求
    @Autowired
    private HttpServletRequest request;

    @Override
    public R getCaptcha(String toEmail) {
        boolean isEmail = Validator.isEmail(toEmail);
        if (!isEmail){
            log.warn("发送邮箱验证码失败！收信人邮箱账号不是邮箱：{}",toEmail);
            return R.userError("发送邮箱验证码失败！收信人邮箱账号不是邮箱：【"+toEmail+"】");
        }
        String ipAddr = IpUtil.getIpAddr(request);
        String emailAndIp = toEmail+":"+ipAddr;
        //监控同一IP同一邮箱   发送验证码数量
        Integer sendCount = (Integer) redisTemplate.opsForValue().get(emailAndIp);
        if(!EmptyUtil.empty(sendCount)){
            //目前记录数量还没有用，等后期看看
            redisTemplate.opsForValue().increment(emailAndIp,1);
            return R.userError("当前验证码还没失效");
        }

        String captcha = RandomUtil.randomString(6);
        EmailDTO emailDTO = new EmailDTO();
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariable("captcha", captcha);
        context.setVariable("expireTime", 2);
        String html = templateEngine.process("captcha", context);
        emailDTO.setToEmail(toEmail);
        emailDTO.setSubject("PaperFlyImageShare邮箱验证码");
        emailDTO.setHtmlOrTxt(true);
        emailDTO.setContent(html);
        emailService.sendEmail(emailDTO);
        redisTemplate.opsForValue().set("Email_Captcha_" + toEmail, captcha);
        redisTemplate.opsForValue().set(emailAndIp,1);
        //2分钟过期时间
        redisTemplate.expire("Email_Captcha_" + toEmail, 2, TimeUnit.MINUTES);
        redisTemplate.expire(emailAndIp, 2, TimeUnit.MINUTES);
        return R.ok("邮箱验证码发送成功,有效期2分钟");
    }

    @Override
    public boolean verifyCaptcha(String email, String captchaCode) {
        if(!Validator.isEmail(email)||EmptyUtil.empty(captchaCode)){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("Email_Captcha_" + email);
        if (EmptyUtil.empty(redisCaptcha)) {
            return false;
        }
        return redisCaptcha.equalsIgnoreCase(captchaCode);
    }

    @Override
    public Boolean removeCaptcha(String uniqueIdentification) {
        if(!Validator.isEmail(uniqueIdentification)){
            return false;
        }
        return redisTemplate.delete(uniqueIdentification);
    }


}
