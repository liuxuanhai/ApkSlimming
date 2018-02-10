package com.pf.res_guard.obfuscate;

import com.pf.res_guard.Contants;
import com.pf.res_guard.arsc.ARSC;
import com.pf.res_guard.arsc.StringPoolRef;
import com.pf.res_guard.codec.ARSCDecoder;
import com.pf.res_guard.utils.FileUtils;
import com.pf.res_guard.utils.ZipUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaopf
 * @version 1.0
 */
public class Obfuscater {

    private ARSC mArsc;

    private File mApkFile;
    private List<String> mSevenZips;

    /**
     * 可用于生成mapping文件
     */
    /**
     * 记录类型混淆前后集合
     */
    public Map<String, String> mTypeMap = new HashMap<>();
    /**
     * 记录名称混淆前后集合
     */
    public Map<String, String> mSpecMap = new HashMap<>();

    public Obfuscater(List<String> sevenZips, File apkFile) {
        mSevenZips = sevenZips;
        mApkFile = apkFile;
    }

    /**
     * 获得res/xxx/xx混淆后的字符串
     *
     * @param string
     * @return
     */
    private String getTableString(String string) {
        // 是res开头的 可能是 res/layout/activity_main.xml
        String newString = string;
        if (string.startsWith("res")) {
            String[] names = string.split("/");
            if (names != null && names.length == 3) {
                // 新名称
                String[] newNames = new String[3];
                // res 变成 r
                newNames[0] = "r";
                newNames[1] = mTypeMap.get(names[1]);
                // 获得名字与后缀
                int index = names[2].indexOf('.');
                String suffix = "";
                if (index > 0) {
                    suffix = names[2].substring(index, names[2].length());
                    names[2] = names[2].substring(0, index);
                }
                newNames[2] = mSpecMap.get(names[2]);
                // 混淆后的名字
                newString = newNames[0] + "/" + newNames[1] + "/" + newNames[2] + suffix;
            }
        }
        return newString;
    }

    /**
     * 获得资源名混淆后的名称
     *
     * @param string
     * @return
     */
    private String getSpecString(String string) {
        String newString = mSpecMap.get(string);
        return newString;
    }

    /**
     * 编码生成混淆后的字符串池
     *
     * @param rawStringPoolRef 原字符串池
     * @param isTable          是否为全局字符串池
     * @return
     */
    private StringPoolRef buildStringPoolRef(StringPoolRef rawStringPoolRef, boolean isTable) {
        try {
            ArrayList<ByteBuffer> strBuffers = new ArrayList<>();
            int strCount = rawStringPoolRef.getStrCount();
            int offset = 0;
            int[] strOffset = new int[strCount];
            // 字符编码的头信息(utf8的字符长度与编码长度) 最长有4个字节
            byte[] strhead = new byte[4];
            for (int i = 0; i < strCount; i++) {
                String string = rawStringPoolRef.getString(i);
                if (isTable) {
                    string = getTableString(string);
                } else {
                    string = getSpecString(string);
                }
                // 使用utf-8编码
                byte[] strData = string.getBytes("UTF-8");
                // 偏移数组
                strOffset[i] = offset;
                // 记录编码使用的长度
                int offsetLen = 0;
                // 字符长度
                if (string.length() < 128) {
                    strhead[offsetLen++] = (byte) (0x7f & (string.length()));
                } else {
                    short len = (short) (string.length());
                    strhead[offsetLen++] = (byte) ((byte) ((len & 0xff00) >> 8) | 0x80);
                    strhead[offsetLen++] = (byte) (len & 0x00ff);
                }
                // 编码后长度
                if (strData.length < 128) {
                    strhead[offsetLen++] = (byte) (0x7f & (strData.length));
                } else {
                    short len = (short) (strData.length);
                    strhead[offsetLen++] = (byte) ((byte) ((len & 0xff00) >> 8) | 0x80);
                    strhead[offsetLen++] = (byte) (len & 0x00ff);
                }
                // 写入bytebuffer
                ByteBuffer buffer = ByteBuffer.allocate(offsetLen + strData.length + 1)
                        .order(ByteOrder.LITTLE_ENDIAN);
                buffer.put(strhead, 0, offsetLen);
                buffer.put(strData);
                // utf-8 一个字节0结尾
                buffer.put((byte) 0);
                buffer.flip();
                strBuffers.add(buffer);
                offset += (offsetLen + strData.length + 1);
            }
            // 获得字符串池数据 (编码完成的)
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (ByteBuffer strBuffer : strBuffers) {
                byte[] data = new byte[strBuffer.limit()];
                strBuffer.get(data);
                bos.write(data);
            }
            byte[] bytes = bos.toByteArray();
            bos.close();
            return new StringPoolRef(strOffset, bytes, rawStringPoolRef.mStyleOffsets,
                    rawStringPoolRef.mStyles, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param dir 路径，带有 /
     * @throws IOException          io错误
     * @throws InterruptedException 错误
     */
    public void obfuscate(String dir) throws IOException, InterruptedException {
        String old_dir_name = Contants.APK_PATH + dir + "/app";
        File old_app = new File(old_dir_name);
        FileUtils.rmdir(old_app);
        // 解压apk 并且获得文件与是否压缩表
        // ZipEntry.STORED 不压缩 0
        // ZipEntry.DEFLATED 压缩 8
        HashMap<String, Integer> compressData = ZipUtils.unZip(mApkFile, old_app);

        /**
         * 练习:指定需要可以压缩的文件 (混淆白名单相同流程)
         * 1、压缩配置中的文件修改压缩方式为 DEFLATED
         *    遍历compressData,对比 mSevenZips 如果符合mSevenZips中的定义则将value修改为 DEFLATED
         * 2、原本没压缩的 -mx=0 重新存入apk
         *    7za a xxx.apk xxxxx -mx=0
         */
        // 配置在压缩表的将value修改成压缩
        // resetCompress(compressData);

        // 解析arsc文件
        mArsc = ARSCDecoder.decode(new File(old_dir_name + "/resources.arsc"));

        // 进行混淆
        obfuscateARSC(mArsc);

        // 码生成混淆后的字符串池
        StringPoolRef tableStringPoolRef = buildStringPoolRef(mArsc.mTableStrings, true);
        StringPoolRef specStringPoolRef = buildStringPoolRef(mArsc.mSpecNames, false);

        // arsc临时文件
        File arscFile = new File(Contants.APK_PATH + dir + "/temp/resources.arsc");
        FileUtils.rmdir(arscFile.getParentFile());
        mArsc.createFile(tableStringPoolRef, specStringPoolRef, arscFile);

        // 修改res下目录与文件名
        obfuscateFile(old_app, arscFile.getParentFile());
        // 压缩apk文件
        File outDir = new File(Contants.APK_PATH + dir);
        // 普通打包
        packing(arscFile.getParentFile(), outDir);
        // 使用7zip打包
        sevenZipPacking(arscFile.getParentFile(), outDir);
        // 生成 mapping 文件
        createMapping(dir);
    }

    /**
     * 生成 mapping 文件
     *
     * @param dir
     */
    private void createMapping(String dir) {
        System.out.println("===生成mapping文件");
        File mappingFile = new File(Contants.APK_PATH + dir + "/mapping", "resources.txt");
        // 先删掉之前的
        if (mappingFile.exists()) {
            mappingFile.delete();
        }
        // 创建文件夹
        if (!mappingFile.getParentFile().exists()) {
            mappingFile.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mappingFile);
            fos.write("===记录类型===".getBytes());
            for (String s : mTypeMap.keySet()) {
                if (mTypeMap.get(s) != null) {
                    fos.write(("\n" + s + " => " + mTypeMap.get(s)).getBytes());
                }
            }
            fos.write("\n===记录名称===".getBytes());
            for (String s : mSpecMap.keySet()) {
                if (mSpecMap.get(s) != null) {
                    fos.write(("\n" + s + " => " + mSpecMap.get(s)).getBytes());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 使用7z 打包apk
     *
     * @param src
     * @param dst
     */
    private void sevenZipPacking(File src, File dst) throws IOException, InterruptedException {
        System.out.println("====使用7zip打包");
        // 打包、对齐与签名
        /**
         * 打包
         */
        String name = mApkFile.getName();
        name = name.substring(0, name.lastIndexOf("."));

        // 获得当前系统
        String os = System.getProperty("os.name");
        String cmd = "";
        if (os.toLowerCase().startsWith("win")) {
            cmd = "cmd /c ";
        }
        /**
         * 使用7z打包
         */
        // 生成极限压缩zip包 -》apk
        File unsigned_unaligned_apk = new File(dst, name + "-7z-unsigned-unaligned.apk");
        unsigned_unaligned_apk.delete();
        String sevenzCmd = cmd + " 7z a -tzip " + unsigned_unaligned_apk.getAbsolutePath()
                + " " + src.getAbsolutePath() + "/* -mx=9";
        System.out.println("7z cmd:" + sevenzCmd);
        Process process = Runtime.getRuntime().exec(sevenzCmd);
        process.waitFor();
        if (process.exitValue() != 0) {
            System.out.println("7z packing error!");
        }
        process.destroy();

        /**
         * 对齐
         */
        // 对齐后的apk
        File unsigned_aligned_apk = new File(dst, name + "-7z-unsigned-aligned.apk");
        unsigned_aligned_apk.delete();
        String zipalignCmd = cmd + " zipalign -f 4 " + unsigned_unaligned_apk.getAbsolutePath()
                + " " + unsigned_aligned_apk.getAbsolutePath();
        process = Runtime.getRuntime().exec(zipalignCmd);
        System.out.println("zipalign cmd:" + zipalignCmd);
        process.waitFor();
        if (process.exitValue() != 0) {
            System.out.println("zipalign error!");
        }
        process.destroy();

        /**
         * 签名
         * apksigner sign             // 执行签名操作
         * --ks jks路径               // jks签名证书路径
         * --ks-key-alias alias       // 生成jks时指定的alias
         * --ks-pass pass:密码        // KeyStore密码
         * --key-pass pass:密码       // 签署者的密码，即生成jks时指定alias对应的密码
         * --out output.apk           // 输出路径
         * input.apk                  // 被签名的apk
         */
        File storeFile = new File(Contants.storeFile);
        File signed_aligned_apk = new File(dst, name + "-7z-signed-aligned.apk");
        signed_aligned_apk.delete();
        String apksignerCmd = cmd + " apksigner sign --ks " + storeFile.getAbsolutePath()
                + " --ks-key-alias " + Contants.keyAlias + " --ks-pass pass:" + Contants.keyPassword
                + " --key-pass pass:" + Contants.storePassword + " --out " + signed_aligned_apk.getAbsolutePath()
                + " " + unsigned_aligned_apk.getAbsolutePath();
        process = Runtime.getRuntime().exec(apksignerCmd);
        System.out.println("apksigner cmd:" + apksignerCmd);
        process.waitFor();
        if (process.exitValue() != 0) {
            System.out.println("apksigner sign error!");
        }
        process.destroy();
    }

    /**
     * 普通打包 使用默认的压缩或者存储 打包apk
     *
     * @param src
     * @param dst
     * @throws Exception
     */
    private void packing(File src, File dst) throws IOException, InterruptedException {
        System.out.println("===普通打包");
        // 打包、对齐与签名
        /**
         * 打包
         */
        String name = mApkFile.getName();
        name = name.substring(0, name.lastIndexOf("."));
        // 打包apk 未签名 未对齐
        File unsigned_unaligned_apk = new File(dst, name + "-unsigned-unaligned.apk");
        unsigned_unaligned_apk.delete();
        ZipUtils.zip(src, unsigned_unaligned_apk);

        // 获得当前系统
        String os = System.getProperty("os.name");
        String cmd = "";
        if (os.toLowerCase().startsWith("win")) {
            cmd = "cmd /c ";
        }
        /**
         * 对齐
         */
        // 对齐后的apk
        File unsigned_aligned_apk = new File(dst, name + "-unsigned-aligned.apk");
        unsigned_aligned_apk.delete();
        String alignCmd = cmd + " zipalign -f 4 " + unsigned_unaligned_apk.getAbsolutePath()
                + " " + unsigned_aligned_apk.getAbsolutePath();
        System.out.println("align cmd:" + alignCmd);
        Process process = Runtime.getRuntime().exec(alignCmd);
        process.waitFor();
        if (process.exitValue() != 0) {
            System.out.println("zipalign error!");
        }
        process.destroy();

        /**
         * 签名
         * apksigner sign             // 执行签名操作
         * --ks jks路径               // jks签名证书路径
         * --ks-key-alias alias       // 生成jks时指定的alias
         * --ks-pass pass:密码        // KeyStore密码
         * --key-pass pass:密码       // 签署者的密码，即生成jks时指定alias对应的密码
         * --out output.apk           // 输出路径
         * input.apk                  // 被签名的apk
         */
        File storeFile = new File(Contants.storeFile);
        File signed_aligned_apk = new File(dst, name + "-signed-aligned.apk");
        signed_aligned_apk.delete();
        String apksignedCmd = cmd + " apksigner sign --ks " + storeFile.getAbsolutePath()
                + " --ks-key-alias " + Contants.keyAlias + " --ks-pass pass:" + Contants.keyPassword
                + " --key-pass pass:" + Contants.storePassword + " --out " + signed_aligned_apk.getAbsolutePath()
                + " " + unsigned_aligned_apk.getAbsolutePath();
        System.out.println("apksigner cmd:" + apksignedCmd);
        process = Runtime.getRuntime().exec(apksignedCmd);
        process.waitFor();
        if (process.exitValue() != 0) {
            System.out.println("apksigner sign error!");
        }
        process.destroy();
    }

    /**
     * 混淆文件名
     *
     * @param src
     * @param dst
     * @throws Exception
     */
    private void obfuscateFile(File src, File dst) throws IOException {
        File[] files = src.listFiles();
        // 把不需要处理的文件拷贝到目的地
        for (File file : files) {
            if (file.getName().equals("resources.arsc")) {
                continue;
            }
            if (file.isFile() || !file.getName().equals("res")) {
                FileUtils.cpFiles(file, new File(dst, file.getName()));
            }
        }
        // 修改res下文件名
        File resDir = new File(src, "res");
        for (File type : resDir.listFiles()) {
            // String newType = mTypeMap.get(type.getName());
            for (File spec : type.listFiles()) {
                String newName = getTableString("res/" + spec.getParentFile().getName() + "/" +
                        spec.getName());
                FileUtils.cpFiles(spec, new File(dst, newName));
            }
        }
    }

    /**
     * 产生混淆名集合
     *
     * @param arsc
     * @throws UnsupportedEncodingException
     */
    private void obfuscateARSC(ARSC arsc) throws UnsupportedEncodingException {
        /**
         * 1、产生全局字符串池中的原名与混淆名集合
         */
        // 类型混淆名生成器
        SimpleNameFactory typeNameFactory = new SimpleNameFactory();
        // 名称混淆名生成器
        SimpleNameFactory specNameFactory = new SimpleNameFactory();
        // 获得全局字符串 string.xml中定义的值 与 res/xx/xx
        int strCount = arsc.mTableStrings.getStrCount();
        for (int i = 0; i < strCount; i++) {
            String rawStr = arsc.mTableStrings.getString(i);
            // 以res开头的 需要混淆
            if (rawStr.startsWith("res")) {
                String[] names = rawStr.split("/");
                // 不符合
                if (names == null || names.length != 3) {
                    continue;
                }
                // 新类型名称
                String newTypeName;
                // 新资源名称
                String newSpecName;
                // 获得混淆名
                newTypeName = mTypeMap.get(names[1]);
                // 此类型还没有混淆后的名称
                if (null == newTypeName) {
                    // 生成混淆名
                    newTypeName = typeNameFactory.nextName();
                    mTypeMap.put(names[1], newTypeName);
                }
                // 获得文件名 (去掉后缀)
                int index = names[2].indexOf('.');
                if (index > 0) {
                    names[2] = names[2].substring(0, index);
                }
                newSpecName = mSpecMap.get(names[2]);
                if (null == newSpecName) {
                    newSpecName = specNameFactory.nextName();
                    mSpecMap.put(names[2], newSpecName);
                }
            }
        }

        /**
         * 2、产生包数据中 类型名称字符串池的原名与混淆名集合
         */
        StringPoolRef mSpecNames = arsc.mSpecNames;
        strCount = mSpecNames.getStrCount();
        for (int i = 0; i < strCount; i++) {
            String specName = mSpecNames.getString(i);
            // 使用全局字符串池的名称映射
            // 如 activity_main 在全局字符串池中有 res/layout/activity_main 需要对应
            // 已经都在mSpecMap了？
            // 不都在,如 全局字符串中 res/layout/strings.xml 在资源名称中没有 只有app_name
            if (!mSpecMap.containsKey(specName)) {
                String newSpecName = specNameFactory.nextName();
                mSpecMap.put(specName, newSpecName);
            }
        }
    }
}