/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.paperfly.imageShare.common.utils.baidu;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Json工具类.
 */
public class GsonUtils {
    private static Gson gson = new GsonBuilder().create();

    public static String toJson(Object value) {
        return gson.toJson(value);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonParseException {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) throws JsonParseException {
        return (T) gson.fromJson(json, typeOfT);
    }


    private static GsonBuilder gsonBuilder = new GsonBuilder();

    static {
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    }

    private static Gson gsonHump = gsonBuilder.create();

    public static String toJson(Object value, Boolean isHump) {
        if(isHump){
            return gsonHump.toJson(value);
        }else {
            return gson.toJson(value);
        }
    }

    public static <T> T fromJson(String json, Class<T> classOfT, Boolean isHump) throws JsonParseException {
        if(isHump){
            return gsonHump.fromJson(json, classOfT);
        }
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT, Boolean isHump) throws JsonParseException {
        if(isHump){
            return (T) gsonHump.fromJson(json, typeOfT);
        }
        return (T) gson.fromJson(json, typeOfT);
    }
}
