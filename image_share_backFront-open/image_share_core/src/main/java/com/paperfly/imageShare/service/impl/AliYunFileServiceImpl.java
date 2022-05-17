package com.paperfly.imageShare.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.dto.ALiYunCredentialsDTO;
import com.paperfly.imageShare.entity.TempFileEntity;
import com.paperfly.imageShare.service.AuthService;
import com.paperfly.imageShare.service.FileService;
import com.paperfly.imageShare.service.TempFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aliyun.oss.internal.OSSConstants.URL_ENCODING;


@Service("aliYunFileService")
@Slf4j
public class AliYunFileServiceImpl implements FileService<Boolean, InputStream> {
    @Autowired
    @Qualifier("aLiYunAuthService")
    AuthService<ALiYunCredentialsDTO> aLiYunAuthService;

    @Value("${aliyun.oss.bucketName}")
    String bucketName;

    @Value("${aliyun.oss.endpoint}")
    String endpoint;

    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;

    @Value("${imageShare.loginBgFolder}")
    String loginBgFolder;


    private Long expireTime = 24 * 3600 * 1000L;

    /**
     * 获取阿里云OSS临时资源访问的URL
     *
     * @param bucketName 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。相当云windows的盘符
     * @param objectName 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。
     * @param endpoint   阿里云的endpoint地点  你的bucketName创建在哪个endpoint（如：oss-cn-beijing.aliyuncs.com）
     * @return
     */
    @Override
    public String getFileUrl(String bucketName, String objectName, String endpoint) {
        //先获取到阿里云的临时访问凭证
        ALiYunCredentialsDTO credentials = aLiYunAuthService.getAuth();
        // 用临时访问凭证,创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentials.getAccessKeyId(), credentials.getAccessKeySecret(), credentials.getSecurityToken());

        // 设置签名URL过期时间为3600*24秒（24小时）。
        Date expiration = new Date(new Date().getTime() + expireTime);
        // 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
        URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
//        System.out.println(url);
        // 关闭OSSClient。
        ossClient.shutdown();
        return url.toString();
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        return this.getFileUrl(bucketName, objectName, endpoint);
    }

    @Override
    public String getFileUrl(String objectName) {
        return this.getFileUrl(bucketName, objectName, endpoint);
    }

    @Override
    public String getFileUrl(String objectName, boolean privateOrPublic) {
        if (privateOrPublic) {
            return getFileUrl(objectName);
        } else {
            //https://image-share-image.oss-cn-beijing.aliyuncs.com/static/bg/login/bg1.jpg
            StringBuffer path = new StringBuffer();
            path.append("https://").append(bucketName).append(".").append(endpoint).append("/").append(objectName);
            return path.toString();
        }
    }

    @Override
    public List<String> getFolderFileUrl(String folderName, boolean privateOrPublic) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ObjectListing objectListing = ossClient.listObjects(bucketName, folderName);

        List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
        List<String> paths = new ArrayList<>();
        if(privateOrPublic){
            for (OSSObjectSummary s : sums) {
                if(loginBgFolder.equals(s.getKey())){
                    continue;
                }
                final String fileUrl = getFileUrl(s.getKey(), true);
                paths.add(fileUrl);
            }
        }else {
            for (OSSObjectSummary s : sums) {
                if(loginBgFolder.equals(s.getKey())){
                    continue;
                }
                final String fileUrl = getFileUrl(s.getKey(), false);
                paths.add(fileUrl);
            }
        }

        // 关闭OSSClient。
        ossClient.shutdown();
        return paths;
    }

    @Override
    public Boolean uploadFiles(List<InputStream> files, List<String> objectNames) {
        if (EmptyUtil.empty(files) || EmptyUtil.empty(objectNames) || files.size() != objectNames.size()) {
            log.error("文件上传为空 或 文件名字为空 或 文件数量和文件名数量不一致！");
            return false;
        }
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        for (int i = 0; i < files.size(); i++) {
            // 依次填写Bucket名称（例如examplebucket）和Object完整路径
            // （例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
            ossClient.putObject(bucketName, objectNames.get(i), files.get(i));
        }
        // 关闭OSSClient。
        ossClient.shutdown();
        return true;
    }



    @Override
    public Boolean uploadFile(InputStream file, String objectName) {
        return uploadFile(file, objectName, CannedAccessControlList.Private);
    }

    @Override
    public Boolean uploadFile(InputStream file, String objectName, CannedAccessControlList access){
        if (EmptyUtil.empty(file) || EmptyUtil.empty(objectName)) {
            log.error("文件上传为空 或 文件名字为空");
            return false;
        }
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //         如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setObjectAcl(access);
        putObjectRequest.setMetadata(metadata);
        ossClient.putObject(putObjectRequest);
        // 关闭OSSClient。
        ossClient.shutdown();
        return true;
    }

    @Override
    public boolean deleteFiles(List<String> objectNames) {
        if (EmptyUtil.empty(objectNames)) {
            log.info("删除文件成功，但好像没有删除成功(文件名集合为空)");
            return true;
        }
        if (objectNames.size() > 1000) {
            log.error("删除文件失败：{}！", "文件个数大于1000个");
            return false;
        }
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(new DeleteObjectsRequest(bucketName)
                .withKeys(objectNames).withEncodingType(URL_ENCODING));
        List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
        try {
            for (String obj : deletedObjects) {
                String deleteObj = URLDecoder.decode(obj, "UTF-8");
                System.out.println(deleteObj);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("删除文件失败，删除文件发生了异常：{}", e.getMessage());
            return false;
        }
        // 关闭OSSClient。
        ossClient.shutdown();
        log.info("删除文件成功！");
        return true;
    }

}
