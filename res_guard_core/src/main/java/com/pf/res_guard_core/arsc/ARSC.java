package com.pf.res_guard_core.arsc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author zhaopf
 * @version 1.0
 * @QQ 1308108803
 * @date 2018/2/9
 */
public class ARSC {

    public File mFile;
    public Header mHeader;
    public StringPoolRef mTableStrings;
    public PackageHeader mPkgHeader;
    public StringPoolRef mTypeNames;
    public StringPoolRef mSpecNames;
    public int mSpecIndex;
    public ByteBuffer remaing;

    public void createFile(StringPoolRef tableStringPoolRef, StringPoolRef specStringPoolRef,
                           File file) throws IOException {
        // 获得新老字符串池长度差
        int tableStrChange = mTableStrings.getSize() - tableStringPoolRef.getSize();
        int specStrChange = mSpecNames.getSize() - specStringPoolRef.getSize();

        System.out.println("原文件大小:" + mHeader.chunkSize);
        // 新文件的大小
        mHeader.chunkSize -= (tableStrChange + specStrChange);
        ByteBuffer buffer = ByteBuffer.allocate(mHeader.chunkSize).order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("新文件大小:" + mHeader.chunkSize);
        // 写入包头
        writeHead(buffer, mHeader);
        // packageNum
        buffer.putInt(1);
        // 写入全局字符串池
        writeStringPool(buffer, tableStringPoolRef);

        // 包数据
        mPkgHeader.mHeader.chunkSize -= specStrChange;
        writePackage(buffer, mPkgHeader);

        // 8104
        System.out.println(mTypeNames.getSize());
        // 类型字符串池
        writeStringPool(buffer, mTypeNames);
        // 资源名称字符串池
        writeStringPool(buffer, specStringPoolRef);

        // 跳到spec开始
        System.out.println("已经写入:" + buffer.position());
        System.out.println("需要写入:" + (buffer.capacity() - buffer.position()));
        System.out.println("余下数据:" + (mFile.length() - mSpecIndex));
        System.out.println("余下数据:" + (remaing.capacity() - remaing.position()));
        buffer.put(remaing);
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(buffer.array());
        fos.flush();
        fos.close();
    }

    public void writePackage(ByteBuffer buffer, PackageHeader packageHeader) throws IOException {
        writeHead(buffer, packageHeader.mHeader);
        buffer.putInt(packageHeader.mPackageId);
        buffer.put(packageHeader.mPackageName);
        buffer.putInt(packageHeader.mTypeStrOffset);
        buffer.putInt(packageHeader.mTypeNameCount);
        buffer.putInt(packageHeader.mSpecNameStrOffset);
        buffer.putInt(packageHeader.mSpecNameCount);
        buffer.putInt(packageHeader.mTypeIdOffset);
    }

    private void writeStringPool(ByteBuffer buffer, StringPoolRef stringPoolRef) throws IOException {
        // 必须被4整除 不足补0
        int add = stringPoolRef.mStrings.length % 4;
        if (add != 0) {
            add = 4 - add;
        }
        // 字符串包头 小端 1c=28 type是1
        buffer.putInt(0x001C0001);
        buffer.putInt(stringPoolRef.getSize());
        // 字符串与style数
        buffer.putInt(stringPoolRef.mStringOffsets.length);
        int styleCount = null == stringPoolRef.mStyleOffsets ? 0 : stringPoolRef.mStyleOffsets.length;
        buffer.putInt(styleCount);
        // 编码
        buffer.putInt(stringPoolRef.isUTF8 ? StringPoolRef.UTF8_FLAG : 0);
        // 字符串数据偏移 包头加两个偏移数组
        buffer.putInt(28 + stringPoolRef.mStringOffsets.length * 4 + styleCount * 4);
        // style数据偏移 包头加两个偏移数组 加字符串数据(需要是4的倍数，可能需要补字节=add个)
        buffer.putInt(styleCount == 0 ? 0 : 28 + stringPoolRef.mStringOffsets.length * 4
                + styleCount * 4 + stringPoolRef.mStrings.length + add);
        // 字符串偏移数组
        writeIntArray(buffer, stringPoolRef.mStringOffsets);
        // style偏移数组
        if (stringPoolRef.mStyleOffsets != null && styleCount != 0) {
            writeIntArray(buffer, stringPoolRef.mStyleOffsets);
        }
        // 字符串数据
        buffer.put(stringPoolRef.mStrings);
        // 补字节
        for (int i = 0; i < add; i++) {
            buffer.put((byte) 0);
        }
        if (stringPoolRef.mStyles != null && stringPoolRef.mStyles.length != 0) {
            buffer.put(stringPoolRef.mStyles);
        }
    }

    public void writeIntArray(ByteBuffer buffer, int[] ints) throws IOException {
        for (int i = 0; i < ints.length; i++) {
            buffer.putInt(ints[i]);
        }
    }

    private void writeHead(ByteBuffer buffer, Header header) throws IOException {
        buffer.putShort(header.type);
        buffer.putShort(header.headerSize);
        buffer.putInt(header.chunkSize);
    }
}