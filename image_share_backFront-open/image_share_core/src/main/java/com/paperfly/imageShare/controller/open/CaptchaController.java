package com.paperfly.imageShare.controller.open;

import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.constant.OperTypeConst;
import com.paperfly.imageShare.common.utils.CaptchaUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.service.CaptchaService;
import com.paperfly.imageShare.service.SensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("open/captcha")
public class CaptchaController {

    @Autowired
    @Qualifier("imageCaptchaService")
    CaptchaService<byte[]> imageCaptchaService;

    @Autowired
    @Qualifier("emailCaptchaService")
    CaptchaService<R> emailCaptchaService;


    @GetMapping(value = "/image/{macCode}",produces = MediaType.IMAGE_JPEG_VALUE)
    @OperLog(operModule = "验证码",operDesc = "获取图片验证码",operType = OperTypeConst.SELECT)
    public byte[] getImageCaptcha(@PathVariable String macCode, HttpServletResponse response) {
        response.addHeader(" Content-Type","image/jpeg");
        return imageCaptchaService.getCaptcha(macCode);
    }

    @GetMapping(value = "/email/{email}")
    @OperLog(operModule = "验证码",operDesc = "获取邮箱验证码",operType = OperTypeConst.SELECT)
    public R getEmailCaptcha(@PathVariable String email) {
        return emailCaptchaService.getCaptcha(email);
    }

}
