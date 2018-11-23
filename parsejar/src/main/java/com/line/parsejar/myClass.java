package com.line.parsejar;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

public class myClass {
    public static void main(String[] args) throws IOException, NotFoundException, CannotCompileException {
        File path = new File("/Users/chenliu/studyApp/GradlePluginTest/parsejar/libs/");

        File[] jarFiles =  path.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.getName().toUpperCase().endsWith("JAR");
            }
        });

//        for (File fileTmp : jarFiles){
//            List<String> jarClass = ScanJarClass.getJarClass(fileTmp.getAbsolutePath());
//            for (String name : jarClass) {
//                System.out.println(name);
//            }
//        }



        ClassPool classPool = ClassPool.getDefault();
        for (File fileTmp : jarFiles){
            System.out.println(fileTmp.getAbsolutePath());
            classPool.insertClassPath(fileTmp.getAbsolutePath());
            List<String> jarClass = ScanJarClass.getJarClass(fileTmp.getAbsolutePath());
            System.out.println(Arrays.toString(jarClass.toArray()));
            String tmpDir = fileTmp.getParent() + "/tmp" + System.currentTimeMillis() + "/";
            for (String className : jarClass) {
                CtClass ctClass = classPool.get(className);
                if (ctClass != null) {
                    CtField ctField = CtField.make("String test = \"test\";", ctClass);
                    ctClass.addField(ctField);
                    ctClass.writeFile(tmpDir);
                }
            }
            File tmpFile = new File(tmpDir);
            String jarName = "n-" + fileTmp.getName();
            String execDes = "jar cvf " +  fileTmp.getParent() + "/" + jarName + " -C " + tmpDir + " .";
            if(tmpFile.exists()){
                Runtime rt = Runtime.getRuntime();
                rt.exec(execDes);
//                FileUtils.deleteFile(tmpDir);
            }
        }

    }






}





















