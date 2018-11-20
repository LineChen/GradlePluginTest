package com.line.fastplugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer

class PluginUtils{

    static boolean isApplicationProject(Project project){
        PluginContainer pluginContainer = project.getPlugins()
        if(pluginContainer.hasPlugin(AppPlugin)){
            return true
        }
        return false
    }

    static boolean isLibraryProject(Project project){
        PluginContainer pluginContainer = project.getPlugins()
        if(pluginContainer.hasPlugin(LibraryPlugin)){
            return true
        }
        return false
    }
}