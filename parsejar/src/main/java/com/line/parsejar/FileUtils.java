package com.line.parsejar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by chenliu on 2018/11/23.
 */

public class FileUtils {
    public static void copyFile(String originPath, String targetPath) throws IOException {
        File originFile = new File(originPath);
        if(originFile.exists()){
            FileInputStream fin = null;
            FileOutputStream fout = null;
            try {
                fin = new FileInputStream(originFile);
                fout = new FileOutputStream(targetPath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fin.read(buffer)) > 0 ){
                    fout.write(buffer, 0, len);
                }
                fout.flush();
            } finally {
                if (fin != null) {
                    fin.close();
                }
                if (fout != null) {
                    fout.close();
                }
            }
        }
    }

    public static void deleteFile(String path){
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
