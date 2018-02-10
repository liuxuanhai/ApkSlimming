package com.pf.res_guard.task

import com.pf.res_guard.Contants
import com.pf.res_guard.extensions.SigningConfigsExtensions
import com.pf.res_guard.obfuscate.Obfuscater
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class GuardTask extends DefaultTask {

    /**
     * apk的全路径
     * apk 全路径集合
     */
    def apkDirs
    /**
     * 输出路径
     */
    @Input
    def outputPath
    /**
     * 签名配置
     */
    @Input
    SigningConfigsExtensions signingConfigs
    /**
     * 7z 的集合
     */
    @Input
    def sevenZDirs
    /**
     * android 中的project
     */
    @Input
    def androidProject

    GuardTask() {
        group = "ResGuard"
        apkDirs = []
        outputPath = ''
        sevenZDirs = []
    }

    @TaskAction
    def run() {
        project.logger.error("------------resGuard build warning --------------")
        apkDirs = getApksPath("${androidProject.buildDir}/outputs/apk")
        project.logger.error("-----apkDirs:${apkDirs}")
        project.logger.error("-----outputPath:${outputPath}")
        project.logger.error("-----signingConfigs:${signingConfigs.toString()}")
        project.logger.error("-----sevenZDirs:${sevenZDirs}")
        project.logger.error("-----androidProject:${androidProject}")

        Contants.APK_PATH = outputPath
        Contants.keyAlias = signingConfigs.keyAlias
        Contants.keyPassword = signingConfigs.keyPassword
        Contants.storeFile = signingConfigs.storeFile
        Contants.storePassword = signingConfigs.storePassword

        apkDirs.each {
            String apkName ->
                String[] apkNames = apkName.split("/")
                if (apkNames.length <= 1) {
                    apkNames = apkName.split("\\\\")
                }
                String dir = apkNames[apkNames.length - 1].split("\\.")[0]
                project.logger.error("-----dir:${dir}")
                File apkFile = new File(apkName)
                if (!apkFile.exists()) {
                    System.out.println(apkName + "  安装包不存在")
                } else {
                    Obfuscater obfuscater = new Obfuscater(sevenZDirs, apkFile)
                    // 混淆会产生apk 和 mapping
                    obfuscater.obfuscate("/" + dir)
                }
        }
        project.logger.error("------------resGuard build finish --------------")
    }

    /**
     * 获取 build/outputs/apk 下的所有apk
     * @param filePath 全路径
     * @return
     */
    def getApksPath(def filePath) {
        project.logger.error("------filePath:${filePath}")
        def apks = []
        def apkPath = new File(filePath)
        if (apkPath.isDirectory()) {
            apkPath.listFiles().each {
                if (it.name.endsWith('.apk'))
                    apks << it.absolutePath
                else if (it.isDirectory()) {
                    getApksPath("${filePath}/${it.name}").each {
                        apks << it
                    }
                }
            }
        } else if (apkPath.name.endsWith('.apk'))
            apks << apkPath.absolutePath
        return apks
    }
}