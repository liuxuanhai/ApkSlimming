package com.pf.res_guard.arsc;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zhaopf
 * @version 1.0
 */
public class Header {

    public short type;
    public short headerSize;
    public int chunkSize;

    public Header(short type, short headSize, int size) {
        this.type = type;
        this.headerSize = headSize;
        this.chunkSize = size;
    }

    public static Header read(ByteBuffer buffer) throws IOException {
        short type = buffer.getShort();
        short headSize = buffer.getShort();
        int chunkSize = buffer.getInt();
        return new Header(type, headSize, chunkSize);
    }

    public final static short TYPE_TABLE = 0x0002;
    public final static short TYPE_PACKAGE = 0x0200;
    public final static short TYPE_TYPE = 0x0202;
    public final static short TYPE_CONFIG = 0x0201;
}