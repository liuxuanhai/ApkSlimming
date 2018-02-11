# 安卓资源混淆插件

## 环境配置

将 `zipalign`、`apksigner`、`7z` 配置到环境变量

1、`zipalign` 和 `apksigner` 在同一个路径: `E:\software\AndroidStudio\SDK\build-tools\26.0.2`

2、 `7z` 在 http://www.7-zip.org/

[windows这里也可以下载](https://github.com/zhaopingfu/ApkSlimming/blob/master/resources/7z1801.exe)

## 使用

    apply plugin: 'com.pf.resguard'

    buildscript {
        buildscript {
            repositories {
                google()
                jcenter()
            }
            dependencies {
                classpath 'com.pf.resGuard:resGuard:1.0.0'
            }
        }
        dependencies {
            classpath 'com.pf.resGuard:resGuard:1.0.0'
        }
    }

    /**
     * 资源混淆配置
     */
    resGuard {
        outputPath "${project.buildDir}/outputs/resGuard"
        sevenZDirs = ['png', 'jpg', 'jpeg', 'gif', 'resources.arsc']
        signingConfigs {
            keyAlias 'androiddebugkey'
            keyPassword 'android'
            storeFile "${project.projectDir}/debug.keystore"
            storePassword 'android'
        }
    }