package com.dommy.qrcode.util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by yhl
 * on 2020/9/4
 * 解码
 */
public class UrlDecodeUtils {

    /**
     * URLDecoder解码
     */
    //    @Test
    public static String toURLDecoder(String paramString) {
        String result = "";
        if (paramString == null || paramString.equals(""))
            return "";
        try {
            String url = new String(paramString.getBytes(), "UTF-8");
            url = URLDecoder.decode(url, "UTF-8");
            result = url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

}
