package com.paperfly.imageShare.service;

import com.aliyun.oss.model.CannedAccessControlList;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileService<T, V> {

    /**
     * 获取AliYUN的OSS存储的临时访问URL
     *
     * @param bucketName 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。相当云windows的盘符
     * @param objectName 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。
     * @param endpoint   阿里云的endpoint地点  你的bucketName创建在哪个endpoint（如：oss-cn-beijing.aliyuncs.com）
     * @return
     */
    String getFileUrl(String bucketName, String objectName, String endpoint);

    String getFileUrl(String bucketName, String objectName);

    /**
     * 获取文件临时URL，文件权限为私有
     * @param objectName 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。相当云windows的盘符
     * @return
     */
    String getFileUrl(String objectName);

    /**
     * 获取文件URL
     * @param objectName 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。相当云windows的盘符
     * @param privateOrPublic 当前文件是私有还是公共读 true:私有，false：公共读
     * @return
     */
    String getFileUrl(String objectName,boolean privateOrPublic);

    /**
     * 获取文件夹中所有文件
     * @param folderName 文件夹名称
     * @param privateOrPublic 是私有还是公共读 true:私有，false：公共读
     * @return
     */
    List<String> getFolderFileUrl(String folderName,boolean privateOrPublic);

    /**
     * 上传文件
     * @param files 文件流
     * @param objectNames 每个文件对应的存储路径+文件名称
     * @return 上传的结果
     * @throws IOException
     */
    T uploadFiles(List<V> files, List<String> objectNames);

    /**
     * 上传单个文件
     * @param file 文件流
     * @param objectName 文件路径+文件名
     * @return 自定义结果
     */
    T uploadFile(V file, String objectName);

    /**
     * 上传文件
     * @param file 文件流
     * @param objectName 文件路径+文件名
     * @param access 文件权限
     * @return 是否上传成功
     */
    T uploadFile(InputStream file, String objectName, CannedAccessControlList access);

    /**
     * 批量删除文件
     * @param objectNames 需要删除的多个文件完整路径。文件完整路径中不能包含Bucket名称（小于1000个）
     * @return 是否删除成功
     */
    boolean deleteFiles(List<String> objectNames);

}
