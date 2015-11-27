package com.farproc.wifi.utils;

/**
 * Created by caiqiqi on 2015/11/24.
 */
public class StringUtil {

    /**
     * is null or its length is 0 or it is made by space
     */
    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }

    /**
     * 将 "ip:port"分割成两个字符串
     * @param str “ip:port”
     * @return
     */
    public static String[] splitStr(String str) throws ArrayIndexOutOfBoundsException{
        String content = str;
        String[] strsContent = content.split(":");
        return strsContent;
    }
}
