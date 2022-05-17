package com.paperfly.imageShare.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.dto.ALiYunCredentialsDTO;
import com.paperfly.imageShare.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 获取阿里云OSS资源访问凭证
 */
@Service("aLiYunAuthService")
@Slf4j
public class ALiYunAuthServiceImpl implements AuthService<ALiYunCredentialsDTO> {
    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpointID}")
    private String endpointID;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public ALiYunCredentialsDTO getAuth() {
        return getAuth(accessKeyId, accessKeySecret);
    }

    @Override
    public ALiYunCredentialsDTO getAuth(String accessKeyId, String accessKeySecret) {
        String redisALiYunCredentialsKey = "ALiYUN_Credentials_Key";
        ALiYunCredentialsDTO aLiYunCredentialsDTO = (ALiYunCredentialsDTO) redisTemplate.opsForValue().get(redisALiYunCredentialsKey);
        if(!EmptyUtil.empty(aLiYunCredentialsDTO)){
            return aLiYunCredentialsDTO;
        }
        //构建一个阿里云客户端，用于发起请求。
        //构建阿里云客户端时需要设置AccessKey ID和AccessKey Secret。
        DefaultProfile profile = DefaultProfile.getProfile(endpointID,
                accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        //构造请求，设置参数。关于参数含义和设置方法，请参见《API参考》。
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysRegionId(endpointID);
        // 填写角色的ARN信息，即需要扮演的角色ID。格式为acs:ram::$accountID:role/$roleName。
        // $accountID为阿里云账号ID。您可以通过登录阿里云控制台，将鼠标悬停在右上角头像的位置，直接查看和复制账号ID，或者单击基本资料查看账号ID。
        // $roleName为RAM角色名称。您可以通过登录RAM控制台，单击左侧导航栏的RAM角色管理，在RAM角色名称列表下进行查看。
        request.setRoleArn("acs:ram::1177289332182132:role/OSS");
        // 自定义角色会话名称，用来区分不同的令牌，例如可填写为SessionTest。
        request.setRoleSessionName("PaperFly_Image_Share_STS");
        Long expireTime = 3600L;
        // 设置临时访问凭证的有效时间为3600秒。
        request.setDurationSeconds(expireTime);

        //发起请求，并得到响应。
        try {
            AssumeRoleResponse response = client.getAcsResponse(request);
            log.info(new Gson().toJson(response));
            AssumeRoleResponse.Credentials credentials = response.getCredentials();
            aLiYunCredentialsDTO = new ALiYunCredentialsDTO();
            BeanUtils.copyProperties(credentials,aLiYunCredentialsDTO);
            redisTemplate.opsForValue().set(redisALiYunCredentialsKey,aLiYunCredentialsDTO);
            redisTemplate.expire(redisALiYunCredentialsKey,expireTime-10, TimeUnit.SECONDS);
            return aLiYunCredentialsDTO;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            log.error("ErrCode:" + e.getErrCode());
            log.error("ErrMsg:" + e.getErrMsg());
            log.error("RequestId:" + e.getRequestId());
        }
        return null;
    }
}
