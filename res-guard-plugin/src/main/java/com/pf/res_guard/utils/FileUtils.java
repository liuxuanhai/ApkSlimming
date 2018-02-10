package com.pf.res_guard.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author zhaopf
 * @version 1.0
 */
public class FileUtils {

    public static void rmdir(File dir) {
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                rmdir(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }

    static void cpFile(File src, File dst) throws IOException {
        if (!src.exists()) {
            return;
        }
        dst.delete();
        dst.getParentFile().mkdirs();
        RandomAccessFile fis = new RandomAccessFile(src, "r");
        byte[] bytes = new byte[(int) fis.length()];
        fis.readFully(bytes);
        fis.close();
        FileOutputStream fos = new FileOutputStream(dst);
        fos.write(bytes);
        fos.close();
        fos.flush();
    }

    public static void cpFiles(File src, File dst) throws IOException {
        if (src.isFile()) {
            cpFile(src, dst);
            return;
        }
        File[] files = src.listFiles();
        for (File file : files) {
            if (file.getName().contains("res_guard_core/build/andres/temp/org/joda/time/tz/data")) {
                System.out.println("1");
            }
            if (file.isDirectory()) {
                cpFiles(file, new File(dst, file.getName()));
                continue;
            }
            cpFile(file, new File(dst, file.getName()));
        }
    }
}