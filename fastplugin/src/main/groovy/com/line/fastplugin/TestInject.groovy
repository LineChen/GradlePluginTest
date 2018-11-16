package com.line.fastplugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor

class TestInject{
    ClassPool classPool = ClassPool.getDefault();

    void injectDir(String path, String dirPath){
        File dir = new File(path)
        classPool.appendClassPath(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->

                String filePath = file.path
                // 这里我们指定修改TestInjectModel.class字节码，在构造函数中增加一行i will inject
                if (filePath.endsWith('.class')
                        && filePath.endsWith('TestInjectModel.class')) {
                    // 判断当前目录是否在我们的应用包里面
                    int index = filePath.indexOf(packageName.replace('.',File.separator))
                    if (index != -1) {
                        int end = filePath.length() - 6 // '.class'.length = 6
                        String className = filePath.substring(index, end)
                                .replace('\\', '.')
                                .replace('/', '.')
                        // 开始修改class文件
                        CtClass ctClass = classPool.getCtClass(className)
                        // 拿到CtClass后可以对 class 做修改操作（addField addMethod ..）
                        if (ctClass.isFrozen()) {
                            ctClass.defrost()
                        }

                        CtConstructor[] constructors = ctClass.getDeclaredConstructors()
                        if (null == constructors || constructors.length == 0) {
                            // 手动创建一个构造函数
                            CtConstructor constructor = new CtConstructor(new CtClass[0], ctClass)
                            constructor.insertBeforeBody(injectStr)
                            //constructor.insertBefore() 会增加super(),且插入的代码在super()前面                            								  ctClass.addConstructor(constructor)
                        } else {
                            constructors[0].insertBeforeBody(injectStr)
                        }
                        ctClass.writeFile(path)
                        ctClass.detach()
                    }
                }
            }
        }

    }
}