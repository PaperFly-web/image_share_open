package com.paperfly.imageShare.service;

/**
 * 服务认证
 */
public interface AuthService<T> {

    T getAuth();

    T getAuth(String ak, String sk);
}
