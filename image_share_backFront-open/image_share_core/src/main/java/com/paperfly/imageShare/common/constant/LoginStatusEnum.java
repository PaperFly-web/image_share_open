package com.paperfly.imageShare.common.constant;

import com.paperfly.imageShare.common.utils.EmptyUtil;

public enum LoginStatusEnum {
    SUCCESS(0,"登录成功"),
    LOGIN_PLACE_CHANGE(1,"系统检测到您常驻城市发生改变,请进行邮箱验证码进一步验证"),
    EMAIL_CAPTCHA_ERROR(2,"邮件验证码错误或过期"),
    IMG_CAPTCHA_ERROR(3,"图片验证码错误或过期"),
    USERNAME_NOT_FOUND(4,"用户未发现"),
    USERNAME_OR_PWD_ERROR(5,"用邮箱或密码错误"),
    USER_LOCK(6,"用户被锁定"),
    CAPTCHA_ERROR(45,"验证码错误"),
    ERROR(99,"登录失败"),
    OTHER_ERROR(100,"未知登录异常"),
    //作为当前的空枚举
    OPTIONAL(-1,"");

    int code;
    String msg;

    LoginStatusEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public LoginStatusEnum getLoginErrorEnuByMsg(String msg){
        if(EmptyUtil.empty(msg)){
            return OPTIONAL;
        }
        for (LoginStatusEnum value : LoginStatusEnum.values()) {
            if(msg.equals(value.getMsg())){
                return value;
            }
        }
        return OPTIONAL;
    }
}
