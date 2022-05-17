/*做一个功能验证，要用到inputStream与outputStream的转换，本以为很简单的东东搞了蛮久，从"程序员 闫帆"处取得一段代码*/
package com.paperfly.imageShare.common.utils.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * inputStream与outputStream的转换
 */
public class StreamConvertUtil {
    /**
     * inputStream转outputStream
     * @param in
     * @return
     * @throws Exception
     */
    public static ByteArrayOutputStream parse(InputStream in) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) != -1) {
            swapStream.write(ch);
        }
        return swapStream;
    }

    /**
     *  outputStream转inputStream
     */
    public static ByteArrayInputStream parse(OutputStream out) throws Exception {
        ByteArrayOutputStream baos;
        baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream;
    }

    /**
     * inputStream转String
     * @param in
     * @return
     * @throws Exception
     */
    public static String parseString(InputStream in) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) != -1) {
            swapStream.write(ch);
        }
        return swapStream.toString();
    }

    /**
     * OutputStream 转String
     * @param out
     * @return
     * @throws Exception
     */
    public static String parseString(OutputStream out) throws Exception {
        ByteArrayOutputStream baos;
        baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream.toString();
    }

    /**
     * String转inputStream
     * @param in
     * @return
     * @throws Exception
     */
    public static ByteArrayInputStream parseInputStream(String in) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(in.getBytes());
        return input;
    }

    /**
     * String 转outputStream
     * @param in
     * @return
     * @throws Exception
     */
    public static ByteArrayOutputStream parseOutputStream(String in) throws Exception {
        return parse(parseInputStream(in));
    }
}