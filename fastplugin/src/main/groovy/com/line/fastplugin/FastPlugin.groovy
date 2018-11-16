package com.line.fastplugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class FastPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "fast";

    @Override
    void apply(Project project) {
        project.extensions.create(EXTENSION_NAME, FastExtension.class)
        project.task('fast') {
            doLast {
                println("Hello From FastPlugin.")
                println(project[EXTENSION_NAME].name)
                println(project[EXTENSION_NAME].version)
            }
        }

        def android = project.extensions.getByType(AppExtension)
        def transform = new FastTransform(project)
        android.registerTransform(transform)
        project.task('transformFast') {
            doLast {
                System.out.println('+++++++++++++++++++++transformFast task')
            }
        }
    }
}