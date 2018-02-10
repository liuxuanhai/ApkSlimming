package com.pf.res_guard.arsc;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PackageHeader {

    public Header mHeader;
    public int mPackageId;
    public byte[] mPackageName = new byte[256];
    public int mTypeStrOffset;
    public int mTypeNameCount;
    public int mSpecNameStrOffset;
    public int mSpecNameCount;
    public int mTypeIdOffset;

    public static PackageHeader read(ByteBuffer buffer) throws IOException {
        PackageHeader header = new PackageHeader();
        header.mHeader = Header.read(buffer);
        // 包id
        header.mPackageId = buffer.getInt();
        // 包名
        buffer.get(header.mPackageName);
        // 类型字符串池偏移 (anim layout drawable等)
        header.mTypeStrOffset = buffer.getInt();
        // lastpublictype 类型字符串资源池的个数
        header.mTypeNameCount = buffer.getInt();
        // 资源项名称字符串池 (app_name activity_main 等)
        header.mSpecNameStrOffset = buffer.getInt();
        // lastpublickey 资源名称字符串资源池的个数
        header.mSpecNameCount = buffer.getInt();
        // 源码中 ResTable_package 还有个 uint32_t typeIdOffset;
        header.mTypeIdOffset = buffer.getInt();
        return header;
    }
}