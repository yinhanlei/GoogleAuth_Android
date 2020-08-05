package com.dommy.qrcode.googleAuth;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by yhl
 * on 2020/8/3
 * 这里是java后台程序。主要功能有，1生成随机秘钥；2生成6位验证码；3在符合偏移量的条件下，对比APP生成的6位验证码。
 * 参考 https://www.jianshu.com/p/de903c074d77
 * 参考 https://www.iteye.com/blog/awtqty-zhang-1986275
 */
public class GoogleAuthenticator {

    public static final int SECRET_SIZE = 10;// 生成的key长度
    public static final String SEED = "g8GjEvTbW5oVSV7avLBdwIHqGlUYNzKFI7izOF8GwLDVKs2m0QN7vxRs2im5MDaNCWGmcD2rvcZx";
    public static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";  // Java实现随机数算法

    /**
     * 生成随机秘钥
     *
     * @return secret key
     */
    public static String generateSecretKey() {
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
            sr.setSeed(Base64.decodeBase64(SEED));
            byte[] buffer = sr.generateSeed(SECRET_SIZE);
            Base32 codec = new Base32();
            byte[] bEncodedKey = codec.encode(buffer);
            String encodedKey = new String(bEncodedKey);
            return encodedKey;
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    /**
     * 生成二维码信息
     *
     * @param user
     * @param host
     * @param secret
     * @return
     */
    public static String getQRBarcodeURL(String user, String host, String secret) {
        String format = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
        return String.format(format, user, host, secret);
    }

    /**
     * 根据秘钥+时间限制获取符合偏移量条件内的验证码，去和APP上生成6位数字验证码做对比。
     * 如果一致，则返回true，不一致
     *
     * @param secret   秘钥
     * @param appCode  APP上生成6位数字验证码
     * @param timeMsec 此刻的时间值
     * @param size     偏移量的值
     * @return
     */
    public static boolean check_code(String secret, long timeMsec, long appCode, int size) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        long t = (timeMsec / 1000L) / 30L;
        int s = 3;//偏移量默认3，最大17
        if (size >= 1 && size <= 17)
            s = size;
        System.out.println("s= " + s);
        for (int i = -s; i <= s; ++i) {
            long code;//此刻后台程序获取的6位数字验证码
            try {
                code = getAuthCode(decodedKey, t + i);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            if (code == appCode) {
                return true;
            }
        }
        return false;
    }

    private static int getAuthCode(byte[] decodedKey, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(decodedKey, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }

    /**
     * APP 上可用下面这个方法去生成6位验证码
     *
     * @param secret
     * @param timeMsec
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static int getAuthCode(String secret, long timeMsec) throws NoSuchAlgorithmException, InvalidKeyException {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        long t = (timeMsec / 1000L) / 30L;

        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(decodedKey, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }
}
