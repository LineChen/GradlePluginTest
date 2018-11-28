package com.line.fastplugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer

import java.util.jar.JarEntry
import java.util.jar.JarFile

class PluginUtils {

    static boolean isApplicationProject(Project project) {
        PluginContainer pluginContainer = project.getPlugins()
        if (pluginContainer.hasPlugin(AppPlugin)) {
            return true
        }
        return false
    }

    static boolean isLibraryProject(Project project) {
        PluginContainer pluginContainer = project.getPlugins()
        if (pluginContainer.hasPlugin(LibraryPlugin)) {
            return true
        }
        return false
    }


    static List<String> getJarClassNames(String jarFilePath) throws IOException {
        List<String> result = new ArrayList<>()

        JarFile jf = new JarFile(new File(jarFilePath))

        Enumeration<JarEntry> enume = jf.entries()
        while (enume.hasMoreElements()) {
            JarEntry element = enume.nextElement()
            String name = element.getName()
            if (name.toUpperCase().endsWith(".CLASS")) {
                result.add(name.replace("/", ".").replace(".class", ""))
            }
        }
        return result
    }

    static void deleteFile(String path){
        File file = new File(path);
        if(file.isDirectory()){
            String[] childs = file.list();
            for (int i = 0; i < childs.length; i++) {

                String p = path + "/" + childs[i];
                System.out.println("delete:" + p);
                deleteFile(p);
            }
        }
        file.delete();
    }
}