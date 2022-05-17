package com.paperfly.imageShare.service.impl;

import com.paperfly.imageShare.dto.EmailDTO;
import com.paperfly.imageShare.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Service("emailService")
public class EmailServiceImpl implements EmailService {
    @Autowired
    JavaMailSenderImpl mailSender;

    @Value("${spring.mail.username}")
    String fromEmail;

    @Override
    public void sendEmail(EmailDTO emailDTO) {
        if (emailDTO.isHtmlOrTxt()) {
            sendHtmlEmail(emailDTO);
        } else {
            sendTxtEmail(emailDTO);
        }
    }

    @Override
    public void sendTxtEmail(EmailDTO emailDTO) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject(emailDTO.getSubject());
        mailMessage.setText(emailDTO.getContent());
        mailMessage.setTo(emailDTO.getToEmail());
        mailMessage.setFrom(fromEmail);
        mailSender.send(mailMessage);
    }

    @Override
    public void sendHtmlEmail(EmailDTO emailDTO) {
        try {
            //这个可以发送带有附件的邮件
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            //设置主题
            helper.setSubject(emailDTO.getSubject());
            //true  设置内容有html语言
            helper.setText(emailDTO.getContent(), true);
            //谁发送给谁
            helper.setTo(emailDTO.getToEmail());
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("发送html类型邮件出现异常！邮件内容：{}", emailDTO);
            e.printStackTrace();
        }

    }
}
