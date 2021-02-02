package com.chris.aop.asm.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AopAsmPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new Exception("AopAsmPlugin must run at application")
        }

        /*****************************************************************************
         * 注册 Transform
         *****************************************************************************/
        def extension = project.extensions.getByType(AppExtension)
        extension.registerTransform(new AopAsmTransform())
    }
}