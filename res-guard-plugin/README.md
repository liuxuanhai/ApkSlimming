# 安卓资源混淆插件

## 使用

    apply plugin: 'com.pf.resguard'

    buildscript {
        repositories {
            google()
            jcenter()
            // jcenter暂时没有通过，只能这样了，通过了就可以把这个删掉了
            maven {
                url 'https://dl.bintray.com/zhaopf/androidlib'
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