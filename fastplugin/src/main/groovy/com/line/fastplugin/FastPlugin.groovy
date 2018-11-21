package com.line.fastplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class FastPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "fast"
    private static final String PLUGIN_APPLICATION = "com.android.application"
    private static final String PLUGIN_LIBRARY = "com.android.library"

    @Override
    void apply(Project project) {
        //just test
        project.extensions.create(EXTENSION_NAME, FastExtension.class)
        project.task('fast') {
            doLast {
                println("Hello From FastPlugin.")
                println(project[EXTENSION_NAME].name)
                println(project[EXTENSION_NAME].version)
            }
        }

        def transform = new FastTransform(project)

        if(PluginUtils.isApplicationProject(project)){
            def android = project.extensions.getByType(AppExtension)
            android.registerTransform(transform)
        }

        if(PluginUtils.isLibraryProject(project)){
            def lib = project.extensions.getByType(LibraryExtension)
            lib.registerTransform(transform)
        }
        //just test
        project.task('transformFast') {
            doLast {
                System.out.println('+++++++++++++++++++++transformFast task')
            }
        }
    }
}