package com.pf.res_guard_core.obfuscate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaopf
 * @version 1.0
 * @QQ 1308108803
 * @date 2018/2/9
 */
public class Client {

    /**
     * 输出 apk 目录
     */
    public static final String APK_PATH = "res_guard_core/build/outputs";

    /**
     * 开始资源混淆
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void guard() throws IOException, InterruptedException {
        // 获得需要强制使用7zip极限压缩的配置
        List<String> sevenZips = readTxtConfig("7zconfig.txt");
        // 获取需要混淆的apk
        List<String> apks = readTxtConfig("apkconfig.txt");
        for (String apkName : apks) {
            if (null == apkName || "".equals(apkName.trim())) {
                continue;
            }
            String[] apkNames = apkName.split("/");
            String dir = apkNames[apkNames.length - 1].split("\\.")[0];
            File apkFile = new File(apkName);
            if (!apkFile.exists()) {
                System.out.println(apkName + "  安装包不存在");
                continue;
            }
            Obfuscater obfuscater = new Obfuscater(sevenZips, apkFile);
            // 混淆会产生apk 和 mapping
            obfuscater.obfuscate("/" + dir);
        }

        //        int[] i = new int[2];
//        i[0] = 0;
//        i[1] = 5;
//        //字符串数据
//        //utf8【编码长度】【字符串长度】【字符串数据】
//        byte[] data = new byte[10];
//        //第一个字符串 data[0]-date[x]
//        //第二个字符串 data[5]-data[x]
    }

    static List<String> readTxtConfig(String fileName) throws IOException {
        ArrayList<String> sevenZips = new ArrayList<>();
        File sevenZipConfigFile = new File("res_guard_core/config/" + fileName);
        FileInputStream fis = new FileInputStream(sevenZipConfigFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line;
        while ((line = reader.readLine()) != null) {
            sevenZips.add(line);
        }
        reader.close();
        return sevenZips;
    }
}