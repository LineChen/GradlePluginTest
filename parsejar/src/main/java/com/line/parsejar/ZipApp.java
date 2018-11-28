package com.line.parsejar;

public class ZipApp {

	public static void main(String[] args) throws Exception {
		String buildRootDir = "/Users/chenliu/studyApp/GradlePluginTest/parsejar/classes/";
		
		String outFile = "/Users/chenliu/studyApp/GradlePluginTest/parsejar/target/target.jar";
		
		//压缩文件
//		FileOutputStream out = new FileOutputStream(outFile);
//		ZipUtil.toZip(buildRootDir, out, true);
		
		//解压缩
//		ZipUtil.decompress(outFile, "/Users/chenliu/eclipse-workspace/");

		JarUtil.toJar(buildRootDir, outFile);

	}

}
