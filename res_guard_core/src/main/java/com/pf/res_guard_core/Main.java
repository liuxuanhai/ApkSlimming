package com.pf.res_guard_core;

import com.pf.res_guard_core.obfuscate.Obfuscater;
import com.pf.res_guard_core.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaopf
 * @version 1.0
 * @QQ 1308108803
 * @date 2018/2/9
 */
public class Main {

    /**
     * 输出 apk 目录
     */
    public static final String APK_PATH = "res_guard_core/build/outputs";

    public static void main(String[] args) throws IOException, InterruptedException {
        // 获得需要强制使用7zip极限压缩的配置
        List<String> sevenZips = read7ZipConfig();

        File apkFile = new File("E:\\software\\AndroidStudio\\projects\\ApkSlimming\\app\\build\\outputs\\apk\\debug\\app-debug.apk");
        if (null == apkFile || !apkFile.exists()) {
            System.out.println("安装包不存在");
            return;
        }
        Obfuscater obfuscater = new Obfuscater(sevenZips, apkFile);
        // 混淆会产生apk 和 mapping
        obfuscater.obfuscate();

//        int[] i = new int[2];
//        i[0] = 0;
//        i[1] = 5;
//        //字符串数据
//        //utf8【编码长度】【字符串长度】【字符串数据】
//        byte[] data = new byte[10];
//        //第一个字符串 data[0]-date[x]
//        //第二个字符串 data[5]-data[x]
    }

    static List<String> read7ZipConfig() throws IOException {
        ArrayList<String> sevenZips = new ArrayList<>();
        File sevenZipConfigFile = new File("res_guard_core/config/7zconfig.txt");
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