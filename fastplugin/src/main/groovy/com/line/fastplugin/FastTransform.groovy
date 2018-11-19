package com.line.fastplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ClassFile
import javassist.bytecode.FieldInfo
import javassist.bytecode.annotation.Annotation
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

class FastTransform extends Transform{

    protected ClassPool pool = ClassPool.getDefault()

    Project project

    private static String ANNOTATION = "com.line.gradleplugintest.FastApi"
    private static String HTTPCREATOR = "com.line.http.HttpCreator"

    FastTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "FastTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }


    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        //transformInvocation.inputs 有两种类型，一种是目录，一种是jar包 分开对其进行遍历
        transformInvocation.inputs.each { TransformInput input ->
            // 对类型为文件夹 的input进行遍历 ：对应的class字节码文件
            // 借用JavaSsist 对文件夹的class 字节码 进行修改
            scanClasses(transformInvocation, input)
            // 对类型为jar的input进行遍历 : 对应三方库等
            input.jarInputs.each { JarInput jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith('.jar')) {
                    jarName = jarName.substring(0, jarName.length() - 4) // '.jar'.length == 4
                }
                File dest = transformInvocation.getOutputProvider().getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                // 将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }

    protected void handleClass(TransformInvocation invocation, DirectoryInput directoryInput) {
        if(directoryInput.file.isDirectory()){
            def root = directoryInput.file.absolutePath
            directoryInput.file.eachFileRecurse { File file ->
                pool.insertClassPath(file.absolutePath)
                if(file.isFile() && !file.getName().contains("R\$") && !file.getName().contains("R.class")){
                    println('handleClass:' + file.absolutePath)
                    def classPath = file.absolutePath.replace(root, "")
                    def className =  classPath.replaceAll("/", ".").replace(".class", "").replaceFirst(".", "")
                    CtClass cc = pool.get(className)
                    if (cc.isFrozen()) {
                        cc.defrost()
                    }

                    if(hasAnnotation(cc, ANNOTATION)){
                        CtField[] declaredFields = cc.getFields()
                        boolean modified = false
                        declaredFields.each {CtField waitEditField->
                            if(hasAnnotation(waitEditField, ANNOTATION)){
                                String fieldType = waitEditField.getType().getName()
                                String fieldName = waitEditField.getName()
                                println("waitEditField:" + fieldType + "," +  fieldName)
                                String makeStr = fieldType + " " + fieldName + " = new " + HTTPCREATOR + "().create(" + fieldType + ".class);"
                                CtField substituteField = CtField.make(makeStr, cc)
                                cc.removeField(waitEditField)
                                cc.addField(substituteField)
                                modified = true
                            }
                        }

                        if(modified){
                            cc.writeFile(directoryInput.file.absolutePath)
                            cc.detach()
                        }
                    }
                }
            }
        }
    }


    static boolean hasAnnotation(CtClass ct, String typeName) {
        ClassFile classFile = ct.getClassFile2()
        AnnotationsAttribute a1 = (AnnotationsAttribute)classFile.getAttribute(AnnotationsAttribute.invisibleTag)
        AnnotationsAttribute a2 = (AnnotationsAttribute)classFile.getAttribute(AnnotationsAttribute.visibleTag)
        return hasAnnotation(typeName, a1, a2)
    }

    static boolean hasAnnotation(CtField ctField, String typeName){
        FieldInfo fi = ctField.getFieldInfo2()
        AnnotationsAttribute a1 = (AnnotationsAttribute)fi.getAttribute(AnnotationsAttribute.invisibleTag)
        AnnotationsAttribute a2 = (AnnotationsAttribute)fi.getAttribute(AnnotationsAttribute.visibleTag)
        return hasAnnotation(typeName, a1, a2)
    }

    static boolean hasAnnotation(String typeName, AnnotationsAttribute a1, AnnotationsAttribute a2) {
        Annotation[] anno1, anno2
        if (a1 == null)
            anno1 = null
        else
            anno1 = a1.getAnnotations()

        if (a2 == null)
            anno2 = null
        else
            anno2 = a2.getAnnotations()

        if (anno1 != null)
            for (int i = 0; i < anno1.length; i++)
                if (anno1[i].getTypeName().equals(typeName))
                    return true

        if (anno2 != null)
            for (int i = 0; i < anno2.length; i++)
                if (anno2[i].getTypeName().equals(typeName))
                    return true

        return false
    }



    /**
     * 扫码local classes，并加入ClassPool的classpath
     */
    protected void scanClasses(TransformInvocation invocation, TransformInput input) {
        input.directoryInputs.each { DirectoryInput directoryInput ->
            if (directoryInput.file.isDirectory()) {
                println "【 directoryInput.file 】" + directoryInput.file
                pool.appendClassPath(getAndroidJarPath())
                def root = directoryInput.file.absolutePath
                pool.insertClassPath(root)
            }
            handleClass(invocation,directoryInput)
            //处理完输入文件之后，要把输出给下一个任务
            def dest = invocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
            FileUtils.copyDirectory(directoryInput.file, dest)
        }
    }

    protected String getAndroidJarPath() {
        def rootDir = project.rootDir
        def localProperties = new File(rootDir, "local.properties")
        def sdkDir = null
        if (localProperties.exists()) {
            Properties properties = new Properties()
            localProperties.withInputStream { instr ->
                properties.load(instr)
            }
            sdkDir = properties.getProperty('sdk.dir')
        }

        def platformsPath = sdkDir + File.separator + "platforms"

        def platformsFile = new File(platformsPath)

        if (platformsFile.exists() && platformsFile.isDirectory() && platformsFile.list().length >= 1) {
            return platformsPath + File.separator + platformsFile.list().sort()[platformsFile.list().size() - 1] + File.separator + "android.jar"
        }
        return ""
    }


}