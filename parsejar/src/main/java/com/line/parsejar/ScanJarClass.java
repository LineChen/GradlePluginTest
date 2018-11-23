package com.line.parsejar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by chenliu on 2018/11/22.
 */

public class ScanJarClass {

    public static List<String> getJarClass(String jarFilePath) throws IOException {
        List<String> result = new ArrayList<>();

        JarFile jf = new JarFile(new File(jarFilePath));

        Enumeration<JarEntry> enume = jf.entries();
        while (enume.hasMoreElements()) {
            JarEntry element = enume.nextElement();
            String name = element.getName();
            if (name.toUpperCase().endsWith(".CLASS")) {
                result.add(name.replace("/", ".").replace(".class", ""));
            }
        }
        return result;
    }


    public static long getCreateTimeFromUrl(String url) {
        if (url == null) return 0;
        try {
            String[] urls = url.split("/");
            System.out.println(Arrays.toString(urls));
            if (urls.length < 5) return 0;

            int p = 4;
            String date = urls[p]; // 20170721
            String time = urls[p + 1].substring(urls[p + 1].lastIndexOf('_') + 1); // 172807

            System.out.println("date:" + date + ", time:" + time);

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            Date d = format.parse(date + time);

            return d.getTime() / 1000;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

}
