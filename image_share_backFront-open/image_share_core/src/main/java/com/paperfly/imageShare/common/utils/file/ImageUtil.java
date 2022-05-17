package com.paperfly.imageShare.common.utils.file;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IoUtil;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.baidu.FileUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片处理工具类  压缩
 */
@Slf4j
public class ImageUtil {
    private static final List<String> imageFileTypes = new ArrayList<>();

    static {
        imageFileTypes.add(ImgUtil.IMAGE_TYPE_JPEG);
        imageFileTypes.add(ImgUtil.IMAGE_TYPE_JPG);
        imageFileTypes.add(ImgUtil.IMAGE_TYPE_PNG);
    }

    /**
     * @param files 文件
     * @param size  压缩到多少字节
     * @return 压缩后的InputStream集合
     * @throws Exception
     */
    public static List<InputStream> compression(MultipartFile[] files, Float size) throws Exception {
        if (EmptyUtil.empty(files)) {
            return null;
        }

        List<InputStream> ins = new ArrayList<>();
        OutputStream outputStream = null;
        BufferedImage buffImage = null;
        ByteArrayInputStream parseIns = null;
        try {
            for (MultipartFile file : files) {
                outputStream = new ByteArrayOutputStream();
                long fileSize = file.getSize();
                //文件小于指定大小，就不压缩
                if (fileSize <= size) {
                    ins.add(file.getInputStream());
                    continue;
                }

                //计算该压缩多少倍
                float outputQuality = size / file.getSize();
                buffImage = ImageIO.read(file.getInputStream());

                Thumbnails.of(buffImage)
                        .size(buffImage.getWidth(), buffImage.getHeight())
                        .outputQuality(outputQuality)
                        .outputFormat(FileUtil.getMultipartFileSuffix(file))
                        .toOutputStream(outputStream);
                parseIns = StreamConvertUtil.parse(outputStream);
                ins.add(parseIns);
            }
        } finally {
            IoUtil.close(outputStream);
        }

        return ins;
    }

    public static InputStream compression(MultipartFile file, Float size) throws Exception {
        if (EmptyUtil.empty(file)) {
            return null;
        }
        OutputStream outputStream = null;
        BufferedImage buffImage = null;
        ByteArrayInputStream parseIns = null;
        try {

            outputStream = new ByteArrayOutputStream();
            long fileSize = file.getSize();
            //文件小于指定大小，就不压缩
            if (fileSize <= size) {
                return file.getInputStream();
            }

            //计算该压缩多少倍
            float outputQuality = size / file.getSize();
            buffImage = ImageIO.read(file.getInputStream());
            Thumbnails.of(buffImage)
                    .size(buffImage.getWidth(), buffImage.getHeight())
                    .outputQuality(outputQuality)
                    .outputFormat(FileUtil.getMultipartFileSuffix(file))
                    .toOutputStream(outputStream);
            parseIns = StreamConvertUtil.parse(outputStream);
            return parseIns;

        } finally {
            IoUtil.close(outputStream);
        }
    }

    /**
     * 检查是否为图片类型
     *
     * @param files 文件流
     * @return true:是 false:不是
     */
    public static boolean isImage(MultipartFile[] files) {
        return FileUtil.isAppointFileType(files, imageFileTypes);
    }

    /**
     * 检查是否为图片类型
     *
     * @param file 文件流
     * @return true:是 false:不是
     */
    public static boolean isImage(MultipartFile file) {
        if (EmptyUtil.empty(file)) {
            return false;
        }
        final MultipartFile[] files = new MultipartFile[1];
        files[0] = file;
        return FileUtil.isAppointFileType(files, imageFileTypes);
    }

    /**
     * 检查是否为图片类型
     *
     * @param files 文件流
     * @return true:是 false:不是
     */
    public static boolean isImage(List<MultipartFile> files) {
        if (EmptyUtil.empty(files)) {
            return false;
        }
        MultipartFile[] filesArr = new MultipartFile[files.size()];
        final MultipartFile[] multipartFiles = files.toArray(filesArr);
        return FileUtil.isAppointFileType(multipartFiles, imageFileTypes);
    }


    /**
     * 按照中心位置，裁剪成正方形
     * @param file
     * @return
     */
    public static InputStream cutIntoSquare(InputStream file) {
        if (EmptyUtil.empty(file)) {
            return null;
        }
        OutputStream outputStream = null;
        BufferedImage buffImage = null;
        ByteArrayInputStream parseIns = null;
        try {
            buffImage = ImageIO.read(file);
            int size = Math.min(buffImage.getWidth(), buffImage.getHeight());
            Thumbnails.of(buffImage).sourceRegion(Positions.CENTER, size, size);
            parseIns = StreamConvertUtil.parse(outputStream);
        } catch (Exception e) {
            log.error(e.getMessage());
        }finally {
            IoUtil.close(outputStream);
        }

        return parseIns;
    }

    /**
     * 裁剪成正方形，并压缩
     * @param file
     * @param size
     * @return
     */
    public static InputStream cutIntoSquareAndCompression(MultipartFile file,Double size){
        if (EmptyUtil.empty(file)) {
            return null;
        }
        OutputStream outputStream = null;
        BufferedImage buffImage = null;
        ByteArrayInputStream parseIns = null;
        try {
            buffImage = ImageIO.read(file.getInputStream());
            outputStream = new ByteArrayOutputStream();
            int minSize = Math.min(buffImage.getWidth(), buffImage.getHeight());
            final long fileSize = file.getSize();

            //文件小于指定大小，就不压缩
            if (fileSize > size) {
                //计算该压缩多少倍
                double outputQuality = size / fileSize;
                Thumbnails.of(buffImage)
                        .size(buffImage.getWidth(), buffImage.getHeight())
                        //从中心点开始裁剪
                        .sourceRegion(Positions.CENTER, minSize, minSize)
                        .outputQuality(outputQuality)
                        .outputFormat(FileUtil.getMultipartFileSuffix(file))
                        .toOutputStream(outputStream);
            }else {
                Thumbnails.of(buffImage)
                        .size(buffImage.getWidth(), buffImage.getHeight())
                        //从中心点开始裁剪
                        .sourceRegion(Positions.CENTER, minSize, minSize)
                        .outputFormat(FileUtil.getMultipartFileSuffix(file))
                        .toOutputStream(outputStream);
            }
            parseIns = StreamConvertUtil.parse(outputStream);
        } catch (Exception e) {
            log.error(e.getMessage());
        }finally {
            IoUtil.close(outputStream);
        }

        return parseIns;
    }

}
