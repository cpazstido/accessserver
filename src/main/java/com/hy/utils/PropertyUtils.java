package com.hy.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by cpazstido on 2016/5/10.
 */
public class PropertyUtils {
    /**
     * 指定property文件
     */
    private static final String PROPERTY_FILE = "config.properties";

    /**
     * 根据Key 读取Value
     *
     * @param key
     * @return
     */
    public static String getValue(String key) {
        Properties props = new Properties();
        try {
            ClassLoader system =  ClassLoader.getSystemClassLoader();
            URL url=system.getResource(PROPERTY_FILE);
            InputStream in = new BufferedInputStream(new FileInputStream(url.getFile()));
            props.load(in);
            in.close();
            String value = props.getProperty(key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
