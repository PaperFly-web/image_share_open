package com.paperfly.imageShare.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmailDTO {
    /**
     * 接收邮件人的email
     */
    private String toEmail;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 发送的是txt还是html的邮件 true:html;false:txt
     */
    private boolean htmlOrTxt;
}
