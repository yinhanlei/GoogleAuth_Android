package com.yhl.authenticator.googleAuth;

import com.yhl.authenticator.googleAuth.GoogleAuthenticator;

import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yhl
 * on 2020/8/3
 */
public class AuthTest {

    /**
     * 生成秘钥和二维码网址
     */
    //    @Test
    public void genSecretTest() {
        String secret = GoogleAuthenticator.generateSecretKey();
        String url = GoogleAuthenticator.getQRBarcodeURL("testuser", "testhost", secret);
        System.out.println("Please register= " + url);
        System.out.println("Secret key= " + secret);
    }

    /**
     * 验证APP生成的code和java后台程序生成的程序code是否一致
     */
    //    @Test
    public void authTest() {
        //1测试时，把genSecretTest方法生成的secret值赋值给它
        String savedSecret = "F366ZY5HHAFQKENQ";
        //2测试时，把code值替换成APP身份验证器上的6位数字
        long appCode = 772858;

        long timeMsec = System.currentTimeMillis();//当前时间

        boolean r = GoogleAuthenticator.check_code(savedSecret, timeMsec, appCode, 5);//设置偏移量为5
        System.out.println("Check code= " + r);
    }

    /**
     * 根据savedSecret秘钥值，获取此刻生成的6位验证码
     */
    @Test
    public void getAuthCodeTest() {
        String savedSecret = "F366ZY5HHAFQKENQ";
        long timeMsec = System.currentTimeMillis();

        int code = 0;
        try {
            code = GoogleAuthenticator.getAuthCode(savedSecret, timeMsec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        System.out.println("Check code= " + code);
    }

}
