package com.pf.res_guard.codec;

import java.io.UnsupportedEncodingException;

/**
 * @author zhaopf
 * @version 1.0
 */
public class StringCodec {

    public static String decodeString(int index, byte[] array, boolean isUTF8) throws
            UnsupportedEncodingException {
        // 对于utf-8编码 有两个长度 一个字符长度与一个编码长度
        // 对于utf-16 只有一个
        // 具体参考 google的开源工程
        // https://github.com/google/android-arscblamer/blob/master/java/com/google/devrel
        // /gmscore/tools/apk/com.dongnao.res.arsc/ResourceString.java
        // UTF-8字符串以0x00结尾，UTF-16字符串以0x0000结尾
        // 这里不需要就不用管了
        if (isUTF8) {
            // 字符串长度
            int strlen = array[index++] & 0xFF;
            if ((strlen & 0x80) != 0) {
                strlen = ((strlen & 0x7F) << 8) | (array[index++] & 0xFF);
            }
            // 编码长度
            int encodelen = array[index++] & 0xFF;
            if ((encodelen & 0x80) != 0) {
                encodelen = ((encodelen & 0x7F) << 8) | (array[index++] & 0xFF);
            }

            byte[] strBytes = new byte[encodelen];
            System.arraycopy(array, index, strBytes, 0, encodelen);
            return new String(strBytes, "UTF-8");
        } else {
            int aShort = getShort(array, index);
            index += 2;
            int length = (aShort & 0xFFFF);
            if ((length & 0x8000) != 0) {
                aShort = getShort(array, index);
                index += 2;
                length = ((length & 0x7FFF) << 16) | (aShort & 0xFFFF);
            }
            // 字符串编码长度
            length *= 2;
            byte[] strBytes = new byte[length];
            System.arraycopy(array, index, strBytes, 0, length);
            return new String(strBytes, "UTF_16LE");
        }
    }

    private static int getShort(byte[] array, int offset) {
        return (array[offset + 1] & 0xff) << 8 | array[offset] & 0xff;
    }
}