package com.paperfly.imageShare.common.utils.baidu;

import com.paperfly.imageShare.common.utils.EmptyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 文件读取工具类
 */
@Slf4j
public class FileUtil {

    /**
     * 读取文件内容，作为字符串返回
     */
    public static String readFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }

        if (file.length() > 1024 * 1024 * 1024) {
            throw new IOException("File is too large");
        }

        StringBuilder sb = new StringBuilder((int) (file.length()));
        // 创建字节输入流  
        FileInputStream fis = new FileInputStream(filePath);
        // 创建一个长度为10240的Buffer
        byte[] bbuf = new byte[10240];
        // 用于保存实际读取的字节数  
        int hasRead = 0;
        while ((hasRead = fis.read(bbuf)) > 0) {
            sb.append(new String(bbuf, 0, hasRead));
        }
        fis.close();
        return sb.toString();
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {
            return readInputStreamByBytes(new FileInputStream(file));
        }
    }

    /**
     * 根据InputStream读取byte[] 数组
     */
    public static byte[] readUrlByBytes(String filePath) throws IOException {
        InputStream ins = null;
        try {
            ins = getFileInputStream(filePath);
            return readInputStreamByBytes(ins);
        }finally {
            try {
                if(!EmptyUtil.empty(ins)){
                    ins.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
    public static byte[] readInputStreamByBytes(InputStream ins) throws IOException {
        if(EmptyUtil.empty(ins)){
            throw new NullPointerException("IO流为空！");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(ins);
            short bufSize = 1024;
            byte[] buffer = new byte[bufSize];
            int len1;
            while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                bos.write(buffer, 0, len1);
            }

            byte[] var7 = bos.toByteArray();
            return var7;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException var14) {
                var14.printStackTrace();
            }
            bos.close();
        }
    }

    /*读取网络文件*/
    public static InputStream getFileInputStream(String path) {
        URL url = null;
        HttpURLConnection conn = null;
        try {
            url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            return conn.getInputStream();
        } catch (Exception e) {
            log.error("读取网络文件异常:" + path);
        }
        return null;
    }

    /**
     * 获取文件后缀名
     *
     * @param file MultipartFile的文件
     * @return 文件后缀名
     */
    public static String getMultipartFileSuffix(MultipartFile file) {
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        return suffix;
    }

    /**
     * 判断是否为指定的文件类型
     *
     * @param files     文件
     * @param fileTypes 文件类型后缀名（不带点）
     * @return true:是；false：不是
     */
    public static boolean isAppointFileType(MultipartFile[] files, List<String> fileTypes) {
        if(EmptyUtil.empty(files) || EmptyUtil.empty(fileTypes)){
            return false;
        }
        for (MultipartFile file : files) {
            if (!fileTypes.contains(getMultipartFileSuffix(file))) {
                return false;
            }
        }
        return true;
    }



    /**
     * 判断文件是否含有大于等于指定size
     *
     * @param files 文件集合
     * @param size  指定大小 单位byte
     * @return
     */
    public static boolean fileIsGetSize(MultipartFile[] files, Long size) {
        for (MultipartFile file : files) {
            if (file.getSize() >= size) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否含有小于等于指定size
     *
     * @param files 文件集合
     * @param size  指定大小 单位byte
     * @return
     */
    public static boolean fileIsLetSize(MultipartFile[] files, Long size) {
        for (MultipartFile file : files) {
            if (file.getSize() <= size) {
                return true;
            }
        }
        return false;
    }

}
