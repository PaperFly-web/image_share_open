package com.paperfly.imageShare.service.impl;

import cn.hutool.json.JSONObject;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 获取token类
 */
@Service("baiDuAuthService")
@Slf4j
public class BaiDuAuthServiceImpl implements AuthService {

    // 官网获取的 API Key 更新为你注册的
    @Value("${baidu.auth.clientId}")
    String clientId;

    // 官网获取的 Secret Key 更新为你注册的
    @Value("${baidu.auth.clientSecret}")
    String clientSecret;

    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 获取权限token
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    @Override
    public String getAuth() {
        return getAuth(clientId, clientSecret);
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Secret Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    @Override
    public String getAuth(String ak, String sk) {
        String redisBaiDuTokenKey = "BaiDu_Access_Token";
        String accessToken = (String) redisTemplate.opsForValue().get(redisBaiDuTokenKey);
        if(!EmptyUtil.empty(accessToken)){
            return accessToken;
        }
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                log.error(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            log.info("result:" + result);
            JSONObject jsonObject = new JSONObject(result);
            accessToken = (String) jsonObject.get("access_token");
            redisTemplate.opsForValue().set(redisBaiDuTokenKey,accessToken);
            redisTemplate.expire(redisBaiDuTokenKey,29, TimeUnit.DAYS);
            return accessToken;
        } catch (Exception e) {
            log.error("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

}