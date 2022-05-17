package com.paperfly.imageShare.common.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class LoginException extends  AuthenticationServiceException{
    public LoginException(String loginStatus){
        super(loginStatus);
    }

    public static class LoginPlaceChangeException extends LoginException{
        public LoginPlaceChangeException(String msg){
            super(msg);
        }
    }
}
