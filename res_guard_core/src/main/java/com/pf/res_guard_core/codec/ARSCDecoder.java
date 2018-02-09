package com.pf.res_guard_core.codec;

import com.pf.res_guard_core.arsc.ARSC;
import com.pf.res_guard_core.arsc.Header;
import com.pf.res_guard_core.arsc.PackageHeader;
import com.pf.res_guard_core.arsc.StringPoolRef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author zhaopf
 * @version 1.0
 * @QQ 1308108803
 * @date 2018/2/9
 */
public class ARSCDecoder {

    public static ARSC decode(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        ByteBuffer buffer = ByteBuffer.allocate((int) file.length()).order(ByteOrder.LITTLE_ENDIAN);
        int len = 0;
        byte[] bytes = new byte[1024];
        while ((len = is.read(bytes)) != -1) {
            buffer.put(bytes, 0, len);
        }
        is.close();
        buffer.flip();
        ARSC arsc = read(file, buffer);
        return arsc;
    }

    private static ARSC read(File file, ByteBuffer buffer) throws IOException {
        ARSC arsc = new ARSC();
        arsc.mFile = file;
        // 解析头
        arsc.mHeader = Header.read(buffer);
        System.out.println("resources.arsc 文件大小:" + arsc.mHeader.chunkSize);
        int packageNum = buffer.getInt();
        if (packageNum != 1) {
            throw new RuntimeException("package number is:" + packageNum);
        }
        // 全局字符串资源池 包含了在string.xml中定义的值 与 res/xx/xx的路径地址
        arsc.mTableStrings = StringPoolRef.read(buffer);
        System.out.println("全局字符串资源池:");
        for (int i = 0; i < arsc.mTableStrings.getStrCount(); i++) {
            System.out.println("    " + arsc.mTableStrings.getString(i));
        }

        // 包数据
        arsc.mPkgHeader = PackageHeader.read(buffer);
        // 包数据中的类型字符串资源池
        arsc.mTypeNames = StringPoolRef.read(buffer);
        System.out.println("类型字符串资源池:");
        for (int i = 0; i < arsc.mTypeNames.getStrCount(); i++) {
            System.out.println("    " + arsc.mTypeNames.getString(i));
        }
        // 资源名称字符串池
        arsc.mSpecNames = StringPoolRef.read(buffer);
        System.out.println("资源名称字符串资源池:");
        for (int i = 0; i < arsc.mSpecNames.getStrCount(); i++) {
            System.out.println("    " + arsc.mSpecNames.getString(i));
        }
        // type spec开始的地方
        arsc.mSpecIndex = buffer.position();
        arsc.remaing = buffer;
        return arsc;
    }
}