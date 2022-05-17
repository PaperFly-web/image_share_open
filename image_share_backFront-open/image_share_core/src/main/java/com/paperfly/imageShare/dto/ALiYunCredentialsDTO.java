package com.paperfly.imageShare.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 阿里云的OSS资源临时访问凭证DTO
 */
@Data
@ToString
public class ALiYunCredentialsDTO implements Serializable {
    private static final long serialVersionUID = 321656486489324455L;
    /**
     * 资源临时访问凭证的token
     */
    private String securityToken;
    /**
     * 本次资源访问凭证的临时秘钥
     */
    private String accessKeySecret;

    /**
     * 本次临时访问凭证的key
     */
    private String accessKeyId;

    /**
     * 本次临时访问凭证过期时间
     */
    private String expiration;
}
