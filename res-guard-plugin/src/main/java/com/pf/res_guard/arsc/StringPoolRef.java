package com.pf.res_guard.arsc;

import com.pf.res_guard.codec.StringCodec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * @author zhaopf
 * @version 1.0
 */
public class StringPoolRef {

    // 包头数据
    public Header mHeader;
    // 字符串偏移数组
    public int[] mStringOffsets;
    // 字符串
    public byte[] mStrings;
    // style偏移数组
    public int[] mStyleOffsets;
    // style
    public byte[] mStyles;
    // 是否utf-8格式
    public boolean isUTF8;

    public static final int UTF8_FLAG = 0x00000100;

    public StringPoolRef() {
    }

    public StringPoolRef(int[] strOffset, byte[] str, int[] styleOffset, byte[] style, boolean isUTF8) {
        mStringOffsets = strOffset;
        mStrings = str;
        mStyleOffsets = styleOffset == null ? new int[0] : styleOffset;
        mStyles = style == null ? new byte[0] : style;
        this.isUTF8 = isUTF8;
    }

    public static StringPoolRef read(ByteBuffer buffer) throws IOException {
        StringPoolRef block = new StringPoolRef();
        block.mHeader = Header.read(buffer);
        int stringCount = buffer.getInt();
        int styleCount = buffer.getInt();
        int flags = buffer.getInt();
        int stringsOffset = buffer.getInt();
        int stylesOffset = buffer.getInt();
        // 如果标记是 0x00000100 则是utf-8编码
        block.isUTF8 = (flags & UTF8_FLAG) != 0;
        // 字符串偏移数组 每一个条目的值 指向字符串数据(byte数组)下标
        // 从这个下标开始解析这个字符串
        block.mStringOffsets = new int[stringCount];
        for (int i = 0; i < stringCount; i++) {
            block.mStringOffsets[i] = buffer.getInt();
        }
        // 如果存在 style  style偏移数组
        if (styleCount != 0) {
            block.mStyleOffsets = new int[stringCount];
            for (int i = 0; i < stringCount; i++) {
                block.mStyleOffsets[i] = buffer.getInt();
            }
        }

        /**
         * 读取字符串
         */
        // 字符串长度 t1.png
        int size = ((stylesOffset == 0) ? block.mHeader.chunkSize : stylesOffset)
                - stringsOffset;
        // arsc文件 字符串池中的 字符串字节长度必须能被4整除
        if ((size % 4) != 0) {
            throw new RuntimeException("String data size must not multiple of 4");
        }
        block.mStrings = new byte[size];
        buffer.get(block.mStrings);

        // 如果有style 读取style
        if (stylesOffset != 0) {
            size = (block.mHeader.chunkSize - stylesOffset);
            // 一样要被4整除
            if ((size % 4) != 0) {
                throw new RuntimeException("Style data size is not multiple of 4");
            }
            block.mStyles = new byte[size];
            buffer.get(block.mStyles);
        }
        return block;
    }

    /**
     * 字符串个数 就是偏移数组的条目数
     *
     * @return 返回
     */
    public int getStrCount() {
        return mStringOffsets.length;
    }

    /**
     * @param index 下标
     * @return 返回指定位置的字符串
     * @throws UnsupportedEncodingException 错误
     */
    public String getString(int index) throws UnsupportedEncodingException {
        int offset = mStringOffsets[index];
        return StringCodec.decodeString(offset, mStrings, isUTF8);
    }

    public int getSize() {
        // 字符串池块 头长 28
        int size = 28;
        // 字符串偏移数组长
        size += (mStringOffsets.length * 4);
        // style偏移数组长
        if (mStyleOffsets != null && mStyleOffsets.length != 0) {
            size += (mStyleOffsets.length * 4);
        }
        // 字符串数据长
        size += mStrings.length;
        // 字符串数据长必须能被4整除 否则需要补0
        int i = mStrings.length % 4;
        if (i != 0) {
            i = 4 - i;
            size += i;
        }
        // style数据长
        if (mStyles != null && mStyles.length != 0) {
            size += mStyles.length;
        }
        return size;
    }
}