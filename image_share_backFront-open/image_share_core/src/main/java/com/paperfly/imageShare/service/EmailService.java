package com.paperfly.imageShare.service;

import com.paperfly.imageShare.dto.EmailDTO;

import javax.mail.MessagingException;

/**
 * 邮件服务
 */
public interface EmailService {

    /**
     * 发送邮件
     * @param emailDTO 邮件实体类
     */
    void sendEmail(EmailDTO emailDTO);

    /**
     * 发送文本邮件
     * @param emailDTO 邮件实体类
     */
    void sendTxtEmail(EmailDTO emailDTO);

    /**
     * 发送HTML类型的邮件
     * @param emailDTO 邮件实体类
     * @throws MessagingException
     */
    void sendHtmlEmail(EmailDTO emailDTO) throws MessagingException;
}
