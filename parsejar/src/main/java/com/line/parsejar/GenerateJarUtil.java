package com.line.parsejar;

import java.io.IOException;

/**
 * Created by chenliu on 2018/11/26.
 */

public class GenerateJarUtil {


    public static void main(String[] args) throws IOException, InterruptedException {

        String currentDir = "/Users/chenliu/studyApp/GradlePluginTest/parsejar";
        String javaSourcePath = currentDir + "/src/main/java/";
        String javaClassPath = currentDir + "/classes";
        String targetPath = currentDir + "/target/MyProject.jar";

        CompilerAndJarTools cl = new CompilerAndJarTools(javaSourcePath, javaClassPath, targetPath);
        cl.complier();
//        cl.generateJar();
    }
}
