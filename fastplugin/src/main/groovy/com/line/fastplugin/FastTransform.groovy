package com.line.fastplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.google.common.collect.Sets
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ClassFile
import javassist.bytecode.FieldInfo
import javassist.bytecode.annotation.Annotation
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

class FastTransform extends Transform {

    protected ClassPool pool = ClassPool.getDefault()

    Project project

    private static String ANNOTATION = "com.line.libuse.api.FastApi"
    private static String HTTPCREATOR = "com.line.libuse.http.HttpCreator"

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
        if (PluginUtils.isApplicationProject(project)) {
            return TransformManager.SCOPE_FULL_PROJECT
        } else if (PluginUtils.isLibraryProject(project)) {
            return Sets.immutableEnumSet(QualifiedContent.Scope.SUB_PROJECTS)
        }
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
            scanJars(transformInvocation, input)
        }
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
            handleClassFileInput(invocation, directoryInput)
            //处理完输入文件之后，要把输出给下一个任务
            def dest = invocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
            FileUtils.copyDirectory(directoryInput.file, dest)
        }
    }

    /**
     * 扫码jar，并加入ClassPool的classpath
     */
    protected void scanJars(TransformInvocation invocation, TransformInput input) {
        input.jarInputs.each { JarInput jarInput ->
            println "【 jarInput.file 】" + jarInput.file.getAbsolutePath()
            pool.insertClassPath(jarInput.file.getAbsolutePath())


            boolean modified = false
            String nJarname = jarInput.file.getName()
            String outputJar = jarInput.file.getParent() + "/" + nJarname


            if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                // ...对jar进行插入字节码
                if (jarInput.file.absolutePath.startsWith(project.rootDir.absolutePath)) {
                    println("【handle Jar】" + jarInput.file.absolutePath)
                    List<String> classNames = PluginUtils.getJarClassNames(jarInput.file.absolutePath)
                    println(Arrays.toString(classNames.toArray()))
                    String tmpDir = jarInput.file.getParent() + "/tmp" + System.currentTimeMillis() + "/"
                    for (String className : classNames) {
                        CtClass cc = pool.get(className)
                        if (cc.isFrozen()) {
                            cc.defrost()
                        }
                        if (hasAnnotation(cc, ANNOTATION)) {
                            CtField[] declaredFields = cc.getFields()
                            declaredFields.each { CtField waitEditField ->
                                if (hasAnnotation(waitEditField, ANNOTATION)) {
                                    String fieldType = waitEditField.getType().getName()
                                    String fieldName = waitEditField.getName()
                                    println("waitEditField:" + fieldType + "," + fieldName)
                                    String makeStr = fieldType + " " + fieldName + " = new " + HTTPCREATOR + "().create(" + fieldType + ".class);"
                                    CtField substituteField = CtField.make(makeStr, cc)
                                    cc.removeField(waitEditField)
                                    cc.addField(substituteField)
                                    modified = true
                                }
                            }
                        }
                        cc.writeFile(tmpDir)
                    }


                    if (modified) {
                        JarUtil.toJar(tmpDir, outputJar)
                        PluginUtils.deleteFile(tmpDir)
                    }

                }
            }

            /**
             * 重名输出文件,因为可能同名,会覆盖
             */
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            def dest = invocation.outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
            File newJarFile = new File(outputJar)
            if (modified) {
                if (newJarFile.exists() && newJarFile.length() > 0) {
                    FileUtils.copyFile(newJarFile, dest)
                    println("====copy edited jar : ==from==" + outputJar + "==to==" + dest.absolutePath)
                }

            } else {
                FileUtils.copyFile(jarInput.file, dest)
                println("====copy origin jar : ==from==" + jarInput.file + "==to==" + dest.absolutePath)
            }
        }
    }

    protected void handleClassFileInput(TransformInvocation invocation, DirectoryInput directoryInput) {
        if (directoryInput.file.isDirectory()) {
            def root = directoryInput.file.absolutePath
            directoryInput.file.eachFileRecurse { File file ->
                pool.insertClassPath(file.absolutePath)
                if (file.isFile() && !file.getName().contains("R\$") && !file.getName().contains("R.class")) {
                    println('handleClass:' + file.absolutePath)
                    def classPath = file.absolutePath.replace(root, "")
                    def className = classPath.replaceAll("/", ".").replace(".class", "").replaceFirst(".", "")
                    CtClass cc = pool.get(className)
                    if (cc.isFrozen()) {
                        cc.defrost()
                    }

                    if (hasAnnotation(cc, ANNOTATION)) {
                        CtField[] declaredFields = cc.getFields()
                        boolean modified = false
                        declaredFields.each { CtField waitEditField ->
                            if (hasAnnotation(waitEditField, ANNOTATION)) {
                                String fieldType = waitEditField.getType().getName()
                                String fieldName = waitEditField.getName()
                                println("waitEditField:" + fieldType + "," + fieldName)
                                String makeStr = fieldType + " " + fieldName + " = new " + HTTPCREATOR + "().create(" + fieldType + ".class);"
                                CtField substituteField = CtField.make(makeStr, cc)
                                cc.removeField(waitEditField)
                                cc.addField(substituteField)
                                modified = true
                            }
                        }

                        if (modified) {
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
        AnnotationsAttribute a1 = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.invisibleTag)
        AnnotationsAttribute a2 = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag)
        return hasAnnotation(typeName, a1, a2)
    }

    static boolean hasAnnotation(CtField ctField, String typeName) {
        FieldInfo fi = ctField.getFieldInfo2()
        AnnotationsAttribute a1 = (AnnotationsAttribute) fi.getAttribute(AnnotationsAttribute.invisibleTag)
        AnnotationsAttribute a2 = (AnnotationsAttribute) fi.getAttribute(AnnotationsAttribute.visibleTag)
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