package com.paperfly.imageShare.common.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class CaptchaException extends AuthenticationServiceException {
    public CaptchaException(String msg){
        super(msg);
    }

    public static class IMGCaptchaException extends  CaptchaException{
        public IMGCaptchaException(String msg){
            super(msg);
        }
    }
    public static class EmailCaptchaException extends  CaptchaException{
        public EmailCaptchaException(String msg){
            super(msg);
        }
    }
}
