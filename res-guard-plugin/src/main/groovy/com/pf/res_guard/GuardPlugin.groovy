package com.pf.res_guard

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.pf.res_guard.extensions.GuardExtensions
import com.pf.res_guard.extensions.SigningConfigsExtensions
import com.pf.res_guard.task.GuardTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class GuardPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)) {
            throw new GradleException("只能在android application当中使用")
        }
        // 添加配置
        project.extensions.create("resGuard", GuardExtensions)
        project.resGuard.extensions.create("signingConfigs", SigningConfigsExtensions)
        // 在解析完配置之后执行
        project.afterEvaluate {
            project.android.applicationVariants.all {
                BaseVariant variant ->
                    // 创建任务,名字是拼接风味的名字
                    def task = project.tasks.create("resGuard${variant.name.capitalize()}", GuardTask) {
                        // 给任务传参
                        outputPath = project.resGuard.outputPath
                        signingConfigs = project.resGuard.signingConfigs
                        sevenZDirs = project.resGuard.sevenZDirs
                        androidProject = project
                    }
                    // 任务依赖关系
                    project.tasks.findByName("assemble${variant.name.capitalize()}").finalizedBy task
            }
        }
    }
}